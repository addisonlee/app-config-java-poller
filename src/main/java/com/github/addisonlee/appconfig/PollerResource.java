package com.github.addisonlee.appconfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/poller")
public class PollerResource {
	@GET
	@Path("/md5")
	@Produces("application/json")
	public String md5() {
        return "yo papa";
	}
}
