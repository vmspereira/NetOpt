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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


public class Minimax {

	
	/**
	 *
	 * Links lista[from] de Map<dst,weight>
	 * 
	 */
	
	
	/**
	 * Topologia
	 */
	protected final List<Map<Integer, Double>> links;

    public Minimax(final List<Map<Integer, Double>> links) {
        this.links = links;
    }
	
    
    public Minimax(NetworkTopology topology, double[][] loads) {
    	//Construct link list
    	this.links=new LinkedList<Map<Integer, Double>>();
    	int size= topology.getDimension(); 
    	for(int from=0;from<size;from++ ){
    		HashMap<Integer, Double> map=new HashMap<Integer, Double>();
    		links.add(from,map);
    		for(int to=0;to<size;to++ )
    		if(topology.getNetGraph().existEdge(from, to)){
    			double bw = topology.getNetGraph().getEdge(from, to).getBandwidth();
    			double value= bw-loads[from][to];
    			map.put(to, value);
    		}
    	} 
    }
    
    
	protected class Link implements Comparable<Link> {
        public final int src;
        public final int dst;
        public final double wgt;

        public Link(int src, int dst, double wgt) {
            this.src = src;
            this.dst = dst;
            this.wgt = wgt;
        }

        
        public Link(NetEdge link, double usage) {
            this.src = link.getFrom();
            this.dst = link.getTo();
            this.wgt = link.getBandwidth()-usage;
        }
        
        
        @Override
        public int compareTo(Link o) {
            if (o == null || o.wgt < wgt) return 1;
            if (o.wgt > wgt) return -1;
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Link)) return false;
            Link link = (Link) o;
            return (src == link.src && dst == link.dst && wgt == link.wgt);
        }

        @Override
        public int hashCode() {
            int result = src;
            result = 31 * result + dst;
            result = (int) (31 * result + wgt);
            return result;
        }

        @Override
        public String toString() {
            return String.format("Link[%d -> %d : %d]", src, dst, wgt);
        }
    }	
	
	
	
	
	protected static void bfs(
        final List<Map<Integer, Double>> links,
        final boolean[] visits,
        final int[] parents,
        final int src) {
		
		
        // Initialize visits and parents vectors.
        Arrays.fill(visits, false);
        visits[src] = true;
        Arrays.fill(parents, -1);

        // Start iterating over vertices.
        final Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(src);
        while (!queue.isEmpty()) {
            final int curr = queue.poll();
            for (final int next : links.get(curr).keySet())
                if (!visits[next]) {
                    visits[next] = true;
                    parents[next] = curr;
                    queue.add(next);
                }
        }
    }

    /**
     * Extracts the path destined to the given node using provided parents vector.
     */
    protected static List<Integer> extractPath(final int[] parents, final int dst) {
        if (parents[dst] == -1) return null;
        LinkedList<Integer> path = new LinkedList<Integer>();
        path.addFirst(dst);
        for (int parent = parents[dst]; parent != -1; parent = parents[parent])
            path.addFirst(parent);
        return path;
    }

    /**
     * Finds the minimax path between given pair in O(E^2).
     */
    public List<Integer> findMinimaxPath(final int src, final int dst) {
        // Shortcut if the source and the destination are identical.
        if (src == dst) return Lists.newArrayList(src);

        // Create storage for BFS.
        final int n = links.size();
        final boolean[] visits = new boolean[n];
        final int[] parents = new int[n];

        // Check if the source and destination nodes are connected at all.
        bfs(links, visits, parents, src);
        if (parents[dst] == -1) return null;
        List<Integer> path = extractPath(parents, dst);

        // Enqueue links in decreasing cost order.
        final PriorityQueue<Link> pq = new PriorityQueue<>(
                n, Collections.reverseOrder());
        for (int i = 0; i < links.size(); i++)
            for (final int j : links.get(i).keySet())
                pq.add(new Link(i, j, links.get(i).get(j)));

        // Copy initial links to a transient link storage.
        final List<Map<Integer, Double>> transLinks = new ArrayList<>();
        for (final Map<Integer, Double> intLinks : links)
            transLinks.add(Maps.newHashMap(intLinks));

        // Remove links incrementally in decreasing weight order until
        // we disconnect all nodes from the source node.
        for (;;) {
            // Consume the next costly links in the queue.
            for (;;) {
                final Link link = pq.poll();
                transLinks.get(link.src).remove(link.dst);
                if (pq.isEmpty() || pq.peek().wgt != link.wgt) break;
            }

            // Check connectivity.
            bfs(transLinks, visits, parents, src);
            if (parents[dst] != -1) path = extractPath(parents, dst);
            else break;
        }

        // Return the last found shortest path.
        return path;
    }

    /**
     * Finds all minimax paths originating from the given source in O(E^2).
     */
    public List<Integer>[] findMinimaxPaths(final int src) {
        // Create storage for BFS.
        final int n = links.size();
        final boolean[] visits = new boolean[n];
        final int[] parents = new int[n];

        // Check if there are any nodes connected to the given source node.
        @SuppressWarnings("unchecked")
        final List<Integer>[] paths = new List[n];
        Arrays.fill(paths, null);
        paths[src] = Lists.newArrayList(src);
        bfs(links, visits, parents, src);
        final Set<Integer> dsts = new HashSet<>();
        for (int i = 0; i < n; i++)
            if (i != src && parents[i] != -1) {
                paths[i] = extractPath(parents, i);
                dsts.add(i);
            }
        if (dsts.isEmpty()) return paths;

        // Enqueue links in decreasing cost order.
        final PriorityQueue<Link> pq = new PriorityQueue<>(
                n, Collections.reverseOrder());
        for (int i = 0; i < links.size(); i++)
            for (final int j : links.get(i).keySet())
                pq.add(new Link(i, j, links.get(i).get(j)));

        // Copy initial links to a transient link storage.
        final List<Map<Integer, Double>> transLinks = new ArrayList<>();
        for (final Map<Integer, Double> intLinks : links)
            transLinks.add(Maps.newHashMap(intLinks));

        // Remove links incrementally in decreasing weight order until
        // we disconnect all nodes from the source node.
        while (!dsts.isEmpty()) {
            // Consume the next costly links in the queue.
            for (;;) {
                final Link link = pq.poll();
                transLinks.get(link.src).remove(link.dst);
                if (pq.isEmpty() || pq.peek().wgt != link.wgt) break;
            }

            // Check connectivity for each destination node.
            bfs(transLinks, visits, parents, src);
            for (final Iterator<Integer> it = dsts.iterator(); it.hasNext();) {
                final int dst = it.next();
                if (parents[dst] == -1) it.remove();
                else paths[dst] = extractPath(parents, dst);
            }
        }

        // Return the last found shortest path.
        return paths;
    }

    /**
     * Finds minimax paths between all pairs in O(VE^2).
     */
    public List<Integer>[][] findAllMinimaxPaths() {
        // Create storage for BFS.
        final int n = links.size();
        final boolean[] visits = new boolean[n];
        final int[] parents = new int[n];

        // Check if there are any connected nodes.
        @SuppressWarnings("unchecked")
        final List<Integer>[][] paths = new List[n][n];
        for (final List<Integer>[] path : paths) Arrays.fill(path, null);
        for (int i = 0; i < n; i++) paths[i][i] = Lists.newArrayList(i);
        int nConns = 0;
        final List<Set<Integer>> dsts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Set<Integer> intDsts = new HashSet<>();
            dsts.add(intDsts);
            bfs(links, visits, parents, i);
            for (int j = 0; j < n; j++)
                if (j != i && parents[j] != -1) {
                    paths[i][j] = extractPath(parents, j);
                    intDsts.add(j);
                    nConns++;
                }
        }
        if (nConns == 0) return paths;

        // Enqueue links in decreasing cost order.
        final PriorityQueue<Link> pq = new PriorityQueue<>(
                n, Collections.reverseOrder());
        for (int i = 0; i < links.size(); i++)
            for (final int j : links.get(i).keySet())
                pq.add(new Link(i, j, links.get(i).get(j)));

        // Copy initial links to a transient link storage.
        final List<Map<Integer, Double>> transLinks = new ArrayList<>();
        for (final Map<Integer, Double> intLinks : links)
            transLinks.add(Maps.newHashMap(intLinks));

        // Remove links incrementally in decreasing weight order until
        // we disconnect all nodes from the source node.
        while (nConns > 0) {
            // Consume the next costly links in the queue.
            for (;;) {
                final Link link = pq.poll();
                transLinks.get(link.src).remove(link.dst);
                if (pq.isEmpty() || pq.peek().wgt != link.wgt) break;
            }

            // Check connectivity for each pair.
            for (int i = 0; i < n; i++) {
                bfs(transLinks, visits, parents, i);
                for (final Iterator<Integer> it = dsts.get(i).iterator(); it.hasNext();) {
                    final int j = it.next();
                    if (parents[j] == -1) {
                        it.remove();
                        nConns--;
                    }
                    else paths[i][j] = extractPath(parents, j);
                }
            }
        }

        // Return the last found shortest path.
        return paths;
    }


	public double getMaxBandwith(List<Integer> path) {
		int current=path.get(0);
		double bw=-1;
		for(int i=1;i<path.size();i++){
			double d=links.get(current).get(path.get(i));
			if(bw==-1 || d<bw )
				bw=d;
			current=path.get(i);
		}
		return bw;
	}

	
	
}
