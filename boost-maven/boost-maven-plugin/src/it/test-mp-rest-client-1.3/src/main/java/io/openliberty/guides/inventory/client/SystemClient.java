// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::client[]
package io.openliberty.guides.inventory.client;

import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// tag::annotations[]
@Dependent
@RegisterRestClient(baseUri = "http://localhost:9000/system") // Doesn't fit
                                                              // well with using
                                                              // a "boost
                                                              // property",
                                                              // something to
                                                              // look at more.
@RegisterProvider(UnknownUrlExceptionMapper.class)
@RegisterProvider(value = PropertiesReader.class, priority = 2000)
@Path("/properties")
public interface SystemClient {
    // end::annotations[]

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getProperties() throws UnknownUrlException, ProcessingException;
}
// end::client[]
