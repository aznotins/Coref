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
