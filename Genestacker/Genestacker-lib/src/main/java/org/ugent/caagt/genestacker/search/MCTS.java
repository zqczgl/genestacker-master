package org.ugent.caagt.genestacker.search;

import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.io.GenestackerInput;
import org.ugent.caagt.genestacker.search.bb.DefaultSeedLotConstructor;
import org.ugent.caagt.genestacker.search.bb.SeedLotConstructor;
import org.ugent.caagt.genestacker.util.GenestackerConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.ugent.caagt.genestacker.DiploidChromosome;

/**
 * 经过重构和优化的MCTS（蒙特卡洛树搜索）搜索引擎。
 * 该实现专注于解决育种方案优化问题，其核心思想是将搜索树的节点定义为
 * 某一世代的植物种群，并通过启发式驱动的模拟来高效地探索通往理想型的路径。
 * 此版本严格遵守“同代杂交”的核心约束。
 */
public class MCTS extends SearchEngine {

    private final SeedLotConstructor seedLotConstructor;
    private final PopulationSizeTools popSizeTools;
    private final Random random = new Random();

    // 为成功获得理想型的路径设置一个巨大的奖励值
    private static final double IDEOTYPE_REWARD = 10000.0;
    private static final double EXPLORATION_PARAMETER = Math.sqrt(2);

    // --- 内部类定义 ---

    /**
     * 代表MCTS搜索树中一个节点的状态。
     */
    private static class PopulationState {
        private final int generation;
        private final Set<Plant> population;
        private final CrossingScheme schemeHistory;

        public PopulationState(int generation, Set<Plant> population, CrossingScheme schemeHistory) {
            this.generation = generation;
            this.population = population;
            this.schemeHistory = schemeHistory;
        }
    }

    /**
     * MCTS树的节点。
     */
    private static class Node {
        private final PopulationState state;
        private final Node parent;
        private final List<Node> children = new ArrayList<>();
        private int visits = 0;
        private double value = 0.0;
        private final List<ParentPair> untriedActions;

        public Node(PopulationState state, Node parent, List<ParentPair> untriedActions) {
            this.state = state;
            this.parent = parent;
            this.untriedActions = new ArrayList<>(untriedActions);
        }
    }

    /**
     * 代表一对用于杂交或自交的亲本。p2为null时表示自交。
     */
    private static class ParentPair {
        private final Plant p1;
        private final Plant p2;

        public ParentPair(Plant p1, Plant p2) {
            this.p1 = p1;
            this.p2 = (p2 == null) ? p1 : p2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentPair that = (ParentPair) o;
            // Unordered pair comparison
            return (Objects.equals(p1, that.p1) && Objects.equals(p2, that.p2)) ||
                    (Objects.equals(p1, that.p2) && Objects.equals(p2, that.p1));
        }

        @Override
        public int hashCode() {
            // Unordered pair hashing
            return Objects.hash(p1) + Objects.hash(p2);
        }
    }

    public MCTS(GenestackerInput input) {
        super(input);
        this.seedLotConstructor = new DefaultSeedLotConstructor(input.getGeneticMap());
        this.popSizeTools = new DefaultPopulationSizeTools(GenestackerConstants.DEFAULT_SUCCESS_PROB);
    }

    @Override
    protected ParetoFrontier runSearch(long runtimeLimit, int numThreads) throws GenestackerException {
        Set<Plant> initialPopulation = new HashSet<>(initialPlants);
        CrossingScheme initialScheme = createInitialScheme(initialPopulation.iterator().next());
        PopulationState initialState = new PopulationState(0, initialPopulation, initialScheme);
        List<ParentPair> initialActions = generatePossibleActions(initialState);
        Node root = new Node(initialState, null, initialActions);

        int iterations = 5000;
        for (int i = 0; i < iterations && !runtimeLimitExceeded(); i++) {
            Node node = select(root);
            if (!isTerminal(node.state)) {
                node = expand(node);
            }
            double reward = simulate(node);
            backpropagate(node, reward);
        }

        Node bestChild = selectBestMove(root);

        ParetoFrontier frontier = new ParetoFrontier();
        if (bestChild != null && bestChild.state.schemeHistory != null) {
            frontier.register(bestChild.state.schemeHistory);
        } else if (isTerminal(root.state)) {
            frontier.register(root.state.schemeHistory);
        }

        return frontier;
    }

    private Node select(Node node) {
        Node current = node;
        while (current.untriedActions.isEmpty() && !current.children.isEmpty()) {
            current = getBestChildUCT(current);
        }
        return current;
    }

    private Node expand(Node node) {
        if (node.untriedActions.isEmpty()) {
            return node;
        }

        ParentPair action = node.untriedActions.remove(random.nextInt(node.untriedActions.size()));

        try {
            SeedLot offspringSeedLot = seedLotConstructor.cross(action.p1.getGenotype(), action.p2.getGenotype());
            if (offspringSeedLot.getGenotypes().isEmpty()) return node;

            Plant bestOffspring = offspringSeedLot.getGenotypes().stream()
                    .filter(g -> g.equals(ideotype))
                    .map(Plant::new)
                    .findFirst()
                    .orElse(selectBestOffspring(offspringSeedLot, ideotype));

            if (bestOffspring == null) return node;

            CrossingScheme newScheme = createNewSchemeFromAction(node.state.schemeHistory, action, bestOffspring, offspringSeedLot);
            if (newScheme == null) return node;

            Set<Plant> newPopulation = new HashSet<>(Collections.singletonList(bestOffspring));
            PopulationState newState = new PopulationState(node.state.generation + 1, newPopulation, newScheme);
            List<ParentPair> nextActions = generatePossibleActions(newState);

            Node childNode = new Node(newState, node, nextActions);
            node.children.add(childNode);
            return childNode;

        } catch (GenestackerException e) {
            e.printStackTrace();
            return node;
        }
    }

    private double simulate(Node startNode) {
        PopulationState currentState = startNode.state;
        int maxSimulatedGenerations = 15;

        for (int i = 0; i < maxSimulatedGenerations; i++) {
            if (isTerminal(currentState)) break;

            List<ParentPair> actions = generatePossibleActions(currentState);
            if (actions.isEmpty()) break;

            ParentPair bestAction = selectBestActionHeuristically(actions, ideotype);

            try {
                SeedLot offspringSeedLot = seedLotConstructor.cross(bestAction.p1.getGenotype(), bestAction.p2.getGenotype());
                if (offspringSeedLot.getGenotypes().isEmpty()) break;

                Plant bestOffspring = selectBestOffspring(offspringSeedLot, ideotype);
                if (bestOffspring == null) break;

                Set<Plant> nextPopulation = new HashSet<>(Collections.singletonList(bestOffspring));
                currentState = new PopulationState(currentState.generation + 1, nextPopulation, null);

            } catch (GenestackerException e) {
                break;
            }
        }
        return calculateReward(currentState.population, ideotype);
    }

    private void backpropagate(Node node, double reward) {
        Node current = node;
        while (current != null) {
            current.visits++;
            current.value += reward;
            current = current.parent;
        }
    }

    private List<ParentPair> generatePossibleActions(PopulationState state) {
        List<Plant> currentPopulation = new ArrayList<>(state.population);
        Set<ParentPair> actions = new HashSet<>();

        // 自交
        for (Plant p : currentPopulation) {
            actions.add(new ParentPair(p, null));
        }

        // 同代种群内杂交
        for (int i = 0; i < currentPopulation.size(); i++) {
            for (int j = i; j < currentPopulation.size(); j++) { // j从i开始，以包含自交
                actions.add(new ParentPair(currentPopulation.get(i), currentPopulation.get(j)));
            }
        }
        return new ArrayList<>(actions);
    }

    private Plant selectBestOffspring(SeedLot seedLot, Genotype ideotype) {
        return seedLot.getGenotypes().stream()
                .max(Comparator.comparingDouble(g -> genotypeSimilarity(g, ideotype)))
                .map(Plant::new)
                .orElse(null);
    }

    private ParentPair selectBestActionHeuristically(List<ParentPair> actions, Genotype ideotype) {
        return actions.stream()
                .max(Comparator.comparingDouble(pair -> genotypeSimilarity(pair.p1.getGenotype(), ideotype) + genotypeSimilarity(pair.p2.getGenotype(), ideotype)))
                .orElse(actions.get(random.nextInt(actions.size())));
    }

    private double calculateReward(Set<Plant> population, Genotype ideotype) {
        boolean ideotypeFound = population.stream().anyMatch(p -> p.getGenotype().equals(ideotype));
        if (ideotypeFound) {
            return IDEOTYPE_REWARD;
        }
        return population.stream()
                .mapToDouble(p -> genotypeSimilarity(p.getGenotype(), ideotype))
                .max()
                .orElse(0.0);
    }

    private double genotypeSimilarity(Genotype g1, Genotype g2) {
        // 关键修复：检查基因型是否为null来判断是否为虚拟植物
        if (g1 == null || g2 == null || g1.nrOfChromosomes() != g2.nrOfChromosomes()) {
            return 0.0;
        }
        double matchingAlleles = 0;
        double totalAlleles = 0;
        for (int i = 0; i < g1.nrOfChromosomes(); i++) {
            DiploidChromosome c1 = g1.getChromosomes().get(i);
            DiploidChromosome c2 = g2.getChromosomes().get(i);
            totalAlleles += c1.nrOfLoci() * 2.0;
            for (int j = 0; j < c1.nrOfLoci(); j++) {
                if (c1.getHaplotypes()[0].targetPresent(j) == c2.getHaplotypes()[0].targetPresent(j)) matchingAlleles++;
                if (c1.getHaplotypes()[1].targetPresent(j) == c2.getHaplotypes()[1].targetPresent(j)) matchingAlleles++;
            }
        }
        return totalAlleles == 0 ? 0 : matchingAlleles / totalAlleles;
    }

    private boolean isTerminal(PopulationState state) {
        if (state.generation >= 15) return true;
        return state.population.stream().anyMatch(p -> p.getGenotype().equals(ideotype));
    }

    private Node getBestChildUCT(Node node) {
        return Collections.max(node.children, Comparator.comparingDouble(c -> uctValue(c)));
    }

    private Node selectBestMove(Node root) {
        return root.children.stream()
                .filter(c -> c.visits > 0)
                .max(Comparator.comparingDouble(c -> c.value / c.visits))
                .orElse(null);
    }

    private double uctValue(Node node) {
        if (node.visits == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double exploitationTerm = node.value / node.visits;
        double explorationTerm = EXPLORATION_PARAMETER * Math.sqrt(Math.log(node.parent.visits) / node.visits);
        return exploitationTerm + explorationTerm;
    }

    private CrossingScheme createInitialScheme(Plant initialPlant) {
        SeedLot initialSeedLot = new SeedLot(initialPlant.getGenotype());
        SeedLotNode initialSeedLotNode = new SeedLotNode(initialSeedLot, 0);
        PlantNode initialPlantNode = new PlantNode(initialPlant, 0, initialSeedLotNode);
        return new CrossingScheme(popSizeTools, initialPlantNode);
    }

    private CrossingScheme createNewSchemeFromAction(CrossingScheme baseScheme, ParentPair action, Plant childPlant, SeedLot sl) {
        try {
            int parentGen = baseScheme.getNumGenerations();

            PlantNode p1Node = findPlantNodeInScheme(baseScheme, action.p1, parentGen);
            PlantNode p2Node = action.p1.equals(action.p2) ? p1Node : findPlantNodeInScheme(baseScheme, action.p2, parentGen);

            if (p1Node == null || p2Node == null) {
                System.err.println("Error: Could not find parent nodes in the scheme history at generation " + parentGen);
                return null;
            }

            PlantNode p1NodeCopy = p1Node.deepUpwardsCopy();
            PlantNode p2NodeCopy = action.p1.equals(action.p2) ? p1NodeCopy : p2Node.deepUpwardsCopy();

            CrossingNode crossing;
            if (action.p1.equals(action.p2)) {
                crossing = new SelfingNode(p1NodeCopy);
            } else {
                crossing = new CrossingNode(p1NodeCopy, p2NodeCopy);
            }

            SeedLotNode sln = new SeedLotNode(sl, parentGen + 1, crossing);
            PlantNode newFinalPlantNode = new PlantNode(childPlant, parentGen + 1, sln);
            return new CrossingScheme(popSizeTools, newFinalPlantNode);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private PlantNode findPlantNodeInScheme(CrossingScheme scheme, Plant targetPlant, int generation) {
        if (scheme == null || targetPlant == null) return null;

        // 对于第0代，亲本可能不在scheme历史中，而是初始植物
        if (generation == 0) {
            return initialPlants.stream()
                    .filter(p -> p.equals(targetPlant))
                    .map(p -> new PlantNode(p, 0, new SeedLotNode(new SeedLot(p.getGenotype()), 0)))
                    .findFirst()
                    .orElse(null);
        }

        return scheme.getPlantNodesFromGeneration(generation).stream()
                .filter(pn -> !pn.isDummy() && pn.getPlant().equals(targetPlant))
                .findFirst()
                .orElse(null);
    }
}