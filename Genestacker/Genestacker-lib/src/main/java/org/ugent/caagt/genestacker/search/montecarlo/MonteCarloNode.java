package org.ugent.caagt.genestacker.search.montecarlo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.ugent.caagt.genestacker.search.CrossingScheme;

public class MonteCarloNode {

    private CrossingScheme scheme;
    private MonteCarloNode parent;
    private List<MonteCarloNode> children;
    private double numVisits;
    private double totalValue;
    
    // Exploration parameter for UCT
    private static final double EXPLORATION_PARAMETER = Math.sqrt(2);

    public MonteCarloNode(CrossingScheme scheme) {
        this.scheme = scheme;
        this.parent = null;
        this.children = new ArrayList<>();
        this.numVisits = 0;
        this.totalValue = 0;
    }

    public CrossingScheme getScheme() {
        return scheme;
    }

    public MonteCarloNode getParent() {
        return parent;
    }

    public void setParent(MonteCarloNode parent) {
        this.parent = parent;
    }

    public List<MonteCarloNode> getChildren() {
        return children;
    }

    public void addChild(MonteCarloNode child) {
        children.add(child);
        child.setParent(this);
    }

    public double getNumVisits() {
        return numVisits;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void update(double value) {
        numVisits++;
        totalValue += value;
    }

    public boolean isFullyExpanded() {
        // In our case, we might not have a fixed number of children,
        // so we might need a different logic for this.
        // For now, let's assume we expand all possible moves at once.
        return !children.isEmpty();
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Selects the best child node based on the UCT (Upper Confidence bound applied to Trees) formula.
     */
    public MonteCarloNode selectBestChild() {
        return Collections.max(children, Comparator.comparing(c -> uctValue(c)));
    }

    /**
     * Calculates the UCT value for a given node.
     */
    private double uctValue(MonteCarloNode node) {
        if (node.getNumVisits() == 0) {
            return Double.POSITIVE_INFINITY; // Prioritize unvisited nodes
        }
        // UCT formula
        return (node.getTotalValue() / node.getNumVisits()) +
               EXPLORATION_PARAMETER * Math.sqrt(Math.log(this.getNumVisits()) / node.getNumVisits());
    }
}
