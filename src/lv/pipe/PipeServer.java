/*******************************************************************************
 * Copyright 2014,2015 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.pipe;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class PipeServer {
	public static int PORT = 8183;

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equalsIgnoreCase("-port") && i + 1 < args.length) {
				PORT = Integer.parseInt(args[i + 1]);
			}
		}

		Component component = new Component();
		component.getServers().add(Protocol.HTTP, PORT);

		component.getDefaultHost().attach("/pipe", PipeResource.class);
		component.getDefaultHost().attach("/pipe/{query}", PipeResource.class);

		component.start();
	}
}
