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
package pt.uminho.algoritmi.netopt.sdn.floodlight;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.sdn.ITopologyParser;

public class TopologyParser implements ITopologyParser {

	/**
	 * 
	 * 
	 * Links [
	 * {"src-switch":"00:00:00:00:00:00:00:02","src-port":3,"dst-switch":"00:00:00:00:00:00:00:03","dst-port":2,"type":"internal","direction":"bidirectional"},
	 * {"src-switch":"00:00:00:00:00:00:00:01","src-port":2,"dst-switch":"00:00:00:00:00:00:00:02","dst-port":2,"type":"internal","direction":"bidirectional"},
	 * {"src-switch":"00:00:00:00:00:00:00:03","src-port":3,"dst-switch":"00:00:00:00:00:00:00:04","dst-port":2,"type":"internal","direction":"bidirectional"}
	 * ]
	 * 
	 * @throws IOException
	 */

	public NetworkTopology parseNetworkTopopogy(String ip,String port) throws IOException {

		HashMap<String, Integer> nds = new HashMap<String, Integer>();
		ArrayList<NetEdge> edges = new ArrayList<NetEdge>();
		int counter = 0;
		//Supposes no additional security is implemented or https
		String surl = "http://" + ip + ":" + port + "/wm/topology/links/json";

		URL url = new URL(surl);
		InputStream is = url.openStream();
		JsonParser parser = Json.createParser(is);
		int[] edge;
		while (parser.hasNext()) {
			Event e = parser.next();
			if (e == Event.START_OBJECT) {
				edge = new int[2];
				Event e1 = parser.next();
				while (e1 != Event.END_OBJECT) {
					if (e1 == Event.KEY_NAME) {
						String key = parser.getString();
						String value = null;
						e1 = parser.next();
						if (e1 == Event.VALUE_STRING) {
							value = parser.getString();
						}
						if (key.equals("src-switch")) {
							int i;
							if (nds.containsKey(value)) {
								i = nds.get(value);

							} else {
								i = counter;
								nds.put(value, i);
								System.out.println(value+" ->"+i);
								counter++;
							}
							edge[0] = i;
						} else if (key.equals("dst-switch")) {
							int i;
							if (nds.containsKey(value)) {
								i = nds.get(value);

							} else {
								i = counter;
								nds.put(value, i);
								System.out.println(value+" ->"+i);
								counter++;
							}
							edge[1] = i;
						}
					}
					e1 = parser.next();
				}
				NetEdge nedge = new NetEdge(edge[0], edge[1]);
				edges.add(nedge);
			}
		}

		NetNode[] nodes=new NetNode[nds.size()];
		Iterator<String> it=nds.keySet().iterator();
		while(it.hasNext()){
			String key=it.next();
			int i=nds.get(key);
			NetNode node= new NetNode();
			node.setNodeId(i);
			node.getProperties().setProperty("DPID", key);
			nodes[i]=node;
		}
		
		NetEdge[] edg= edges.toArray(new NetEdge[edges.size()]);
		return new NetworkTopology(nodes,edg);
	}


	public static void main(String[] args) {
		TopologyParser parser =new TopologyParser();
		try {
			String ip = SystemConf.getPropertyString("floodlight.ip", "127.0.0.1");
			String port = SystemConf.getPropertyString("floodlight.port", "8080");
			parser.parseNetworkTopopogy(ip,port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
