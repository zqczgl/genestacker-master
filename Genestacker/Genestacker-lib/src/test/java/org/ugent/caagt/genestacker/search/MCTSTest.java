package org.ugent.caagt.genestacker.search;

import org.junit.Test;
import org.ugent.caagt.genestacker.*;
import org.ugent.caagt.genestacker.io.GenestackerInput;
import org.ugent.caagt.genestacker.util.GenestackerConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MCTSTest {

    @Test
    public void testMCTSExecution() throws Exception {
        // Create a simple test case with 2 loci and 2 chromosomes
        GenestackerInput input = createSimpleTestInput();
        
        // Create MCTS engine
        MCTS mcts = new MCTS(input);
        
        // Run the search
        ParetoFrontier frontier = mcts.search(10000, 1); // 10 seconds, 1 thread
        
        // Verify that we got results
        assertNotNull("Frontier should not be null", frontier);
        assertTrue("Frontier should contain solutions", frontier.getFrontier().size() > 0);
        
        System.out.println("MCTS found " + frontier.getFrontier().size() + " solutions");
        
        // Print the best solution
        CrossingScheme best = frontier.getFrontier().iterator().next();
        System.out.println("Best solution has " + best.getNumGenerations() + " generations");
        System.out.println("Population size: " + best.getTotalPopulationSize());
        System.out.println("Linkage phase ambiguity: " + best.getLinkagePhaseAmbiguity());
    }
    
    private GenestackerInput createSimpleTestInput() throws Exception {
        // Create haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{false, false}); // [0 0]
        Haplotype hap2 = new Haplotype(new boolean[]{true, true});   // [1 1]
        Haplotype hap3 = new Haplotype(new boolean[]{false, true});  // [0 1]
        Haplotype hap4 = new Haplotype(new boolean[]{true, false});  // [1 0]
        
        // Create diploid chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap2); // [0 0] / [1 1]
        DiploidChromosome chr2 = new DiploidChromosome(hap3, hap4); // [0 1] / [1 0]
        
        // Create genotypes (plants)
        List<DiploidChromosome> chroms1 = new ArrayList<>();
        chroms1.add(chr1);
        chroms1.add(chr2);
        Genotype plant1 = new Genotype(chroms1);
        
        DiploidChromosome chr3 = new DiploidChromosome(hap2, hap1); // [1 1] / [0 0]
        DiploidChromosome chr4 = new DiploidChromosome(hap4, hap3); // [1 0] / [0 1]
        
        List<DiploidChromosome> chroms2 = new ArrayList<>();
        chroms2.add(chr3);
        chroms2.add(chr4);
        Genotype plant2 = new Genotype(chroms2);
        
        // Ideotype (target genotype)
        DiploidChromosome chr5 = new DiploidChromosome(hap2, hap2); // [1 1] / [1 1]
        DiploidChromosome chr6 = new DiploidChromosome(hap2, hap2); // [1 1] / [1 1]
        
        List<DiploidChromosome> chroms3 = new ArrayList<>();
        chroms3.add(chr5);
        chroms3.add(chr6);
        Genotype ideotype = new Genotype(chroms3);
        
        // Create genetic map with proper distances
        // For 2 loci per chromosome, we need 1 distance value per chromosome
        double[][] distances = new double[][]{
            {20.0}, // 20 cM between the two loci on chromosome 1
            {20.0}  // 20 cM between the two loci on chromosome 2
        };
        GeneticMap map = new GeneticMap(distances, new HaldaneMapFunction());
        
        // Create list of initial plants
        List<Plant> initialPlants = new ArrayList<>();
        initialPlants.add(new Plant(plant1));
        initialPlants.add(new Plant(plant2));
        
        // Create input
        GenestackerInput input = new GenestackerInput(initialPlants, ideotype, map);
        return input;
    }
}