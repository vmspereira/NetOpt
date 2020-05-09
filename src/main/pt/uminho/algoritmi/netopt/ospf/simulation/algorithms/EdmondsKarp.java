/*******************************************************************************
 * Copyright 2012-2017,
 *  Centro Algoritmi - University of Minho
 * 
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Public License for more details.
 * 
 *  You should have received a copy of the GNU Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Vítor Pereira
 ******************************************************************************/
package pt.uminho.algoritmi.netopt.ospf.simulation.algorithms;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class EdmondsKarp {
    public static int edmondsKarp(int[][] E, int[][] C, int s, int t) {
        int n = C.length;
        // Residual capacity from u to v is C[u][v] - F[u][v]
        int[][] F = new int[n][n];
        while (true) {
            int[] P = new int[n]; // Parent table
            Arrays.fill(P, -1);
            P[s] = s;
            int[] M = new int[n]; // Capacity of path to node
            M[s] = Integer.MAX_VALUE;
            // BFS queue
            Queue<Integer> Q = new LinkedList<Integer>();
            Q.offer(s);
            LOOP:
            while (!Q.isEmpty()) {
                int u = Q.poll();
                for (int v : E[u]) {
                    // There is available capacity,
                    // and v is not seen before in search
                    if (C[u][v] - F[u][v] > 0 && P[v] == -1) {
                        P[v] = u;
                        M[v] = Math.min(M[u], C[u][v] - F[u][v]);
                        if (v != t)
                            Q.offer(v);
                        else {
                            // Backtrack search, and write flow
                            while (P[v] != v) {
                                u = P[v];
                                F[u][v] += M[t];
                                F[v][u] -= M[t];
                                v = u;
                            }
                            break LOOP;
                        }
                    }
                }
            }
            if (P[t] == -1) { // We did not find a path to t
                int sum = 0;
                for (int x : F[s])
                    sum += x;
                return sum;
            }
        }
    }
}
