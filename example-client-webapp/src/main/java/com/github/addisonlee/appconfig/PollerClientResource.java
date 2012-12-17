package com.github.addisonlee.appconfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/poller")
public class PollerClientResource {
    @GET
    @Path("/md5")
    @Produces("application/json")
    public String md5() {
        return PollerServletLifeCycleListener.getHash();
    }
}
