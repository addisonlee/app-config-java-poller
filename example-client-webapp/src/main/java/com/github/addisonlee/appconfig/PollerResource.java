package com.github.addisonlee.appconfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

@Path("/poller")
public class PollerResource {
	@GET
	@Path("/md5")
	@Produces("application/json")
	public String md5() throws IOException {
        return PollerService.getHash();
	}

	@GET
	@Path("/stub/md5")
	@Produces("application/json")
	public String stubMd5() throws IOException {
        return PollerService.getStubHash();
	}
}
