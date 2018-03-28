/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uma.jmetalmsa.int_consistency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetalmsa.problem.MSAProblem;
import org.uma.jmetalmsa.problem.SATE_MSAProblem;
import org.uma.jmetalmsa.score.Score;
import org.uma.jmetalmsa.score.impl.EntropyScore;
import org.uma.jmetalmsa.solution.MSASolution;
import org.uma.jmetalmsa.stat.CalculateObjetivesFromVAR;
//import org.uma.jmetalmsa.solution.MSASolution;

/**
 *
 * @author Nayeem
 */
public class RelativeDistance
{

    int refId;
    double[] dist;
    List<Neighbor> neighborList;
    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
    int[] rank;

    public RelativeDistance(int refId, int size)
    {
        this.refId = refId;
        dist = new double[size];
    }

    public void generateRelativeDist(char[][] msa, PairwiseDistance getDist)
    {
        for (int i = 0; i < dist.length; i++)
        {
            if (i == refId)
            {
                dist[i] = 0;
            } else
            {
                dist[i] = getDist.getDistance(msa[refId], msa[i]);
                if (dist[i] < min)
                {
                    min = dist[i];
                }

                if (dist[i] > max)
                {
                    max = dist[i];
                }
            }
        }

        //normalize
        for (int i = 0; i < dist.length; i++)
        {
            double del = max - min;
//            if(i == refId)  
//            {
//                dist[i] = -1;
//            }
            //else
            //{
            dist[i] = (dist[i] - min) / del;
            // }
        }

    }

    public List<Neighbor> calculateSortedNeighbor()
    {
        if (dist == null)
        {
            throw new JMetalException("You need to calculate dist first!!!");
        }
        neighborList = new ArrayList<>();
        for (int i = 0; i < dist.length; i++)
        {
            if (i == refId)
            {
                continue;
            }
            neighborList.add(new Neighbor(i, dist[i]));
        }
        Collections.sort(neighborList);
        //neighborList.remove(0);
        rank = new int[dist.length];

        for (int i = 0; i < neighborList.size(); i++)
        {
            rank[neighborList.get(i).id] = i;
        }
        rank[refId] = -1;
        return neighborList;
    }

    double calculateCloseness1(int[] closeNeigborIndices)
    {
        double sum = 0;
        double min = dist[closeNeigborIndices[0]];
        for (int i = 1; i < closeNeigborIndices.length; i++)
        {
            if (dist[closeNeigborIndices[i]] < min)
            {
                min = dist[closeNeigborIndices[i]];
            }

        }

        for (int i = 0; i < closeNeigborIndices.length; i++)
        {
            sum += (dist[closeNeigborIndices[i]] - min);
        }

        return sum;
    }

    class Neighbor implements Comparable<Neighbor>
    {

        int id;
        double dist;

        public Neighbor(int id, double dist)
        {
            this.id = id;
            this.dist = dist;
        }

        @Override
        public String toString()
        {
            return "Neighbor{" + "id=" + id + ", dist=" + dist + '}';
        }

        @Override
        public int compareTo(Neighbor o)
        {
            return Double.compare(o.dist, this.dist); //Double.compare(this.dist, o.dist);
        }

    }

    public static void main(String[] arg) throws Exception
    {
        String instancePath = "dataset/100S";
        String instanceName = "R0";
        String inputFilePath = "F:\\Phd@CSE,BUET\\Com. Biology\\MSA\\Dataset\\scripts\\input\\NumGaps_SOP\\precomputedInit\\uniqueCombined_R0Small";
        List<Score> scoreList = new ArrayList<>();
        scoreList.add(new EntropyScore());
        MSAProblem problem = new SATE_MSAProblem(instanceName, instancePath, scoreList);
        //numOfSeq = problem.getNumberOfVariables();
        CalculateObjetivesFromVAR ob = new CalculateObjetivesFromVAR();
        List<MSASolution> pop = ob.createPopulationFromEncodedVarFile(inputFilePath, problem);
        char[][] msa = pop.get(4).decodeToMatrix();
        RelativeDistance self = new RelativeDistance(5, pop.get(0).getMSAProblem().getNumberOfVariables());
        self.generateRelativeDist(msa, new PairwiseDistance());
        List<Neighbor> neighborList = self.calculateSortedNeighbor();
        System.out.println(neighborList.get(3));
    }

    double calculateCloseness2(int[] closeNeigborIndices)
    {
        double sum = 0;

        calculateSortedNeighbor();

        double min = dist[closeNeigborIndices[0]];
        double max = dist[closeNeigborIndices[0]];
        for (int i = 1; i < closeNeigborIndices.length; i++)
        {
            if (dist[closeNeigborIndices[i]] < min)
            {
                min = dist[closeNeigborIndices[i]];
            }
            if (dist[closeNeigborIndices[i]] > max)
            {
                max = dist[closeNeigborIndices[i]];
            }
        }
        return max - min;
    }

    double calculateCloseness3(int[] closeNeigborIndices)
    {
        double sum = 0;
        calculateSortedNeighbor();

        double min = rank[closeNeigborIndices[0]];
        double max = rank[closeNeigborIndices[0]];
        for (int i = 1; i < closeNeigborIndices.length; i++)
        {
            if (rank[closeNeigborIndices[i]] < min)
            {
                min = rank[closeNeigborIndices[i]];
            }
            if (rank[closeNeigborIndices[i]] > max)
            {
                max = rank[closeNeigborIndices[i]];
            }
        }

        if ((max - min) == (closeNeigborIndices.length - 1))
        {
            return 1;
        } else
        {
            return 0;
        }
    }

    double calculateFarness1(int[] twoEnds)
    {
        return Math.abs(dist[twoEnds[0]] - dist[twoEnds[1]]);
    }

}