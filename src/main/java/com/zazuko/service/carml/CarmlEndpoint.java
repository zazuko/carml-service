package com.zazuko.service.carml;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;


@RequestScoped
@Path("/")
public class CarmlEndpoint {

	@GET	
	@Produces(MediaType.TEXT_PLAIN)
	@Path("test")
	public String test() {
		return "Hello World";
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/n-triples")
	public Response doConversion(@Multipart(value="mapping") Attachment mapping,@Multipart(value="source") Attachment source) {
		Set<TriplesMap> carmlMapping =
				RmlMappingLoader
				.build().load(RDFFormat.TURTLE, new ByteArrayInputStream(mapping.getObject(String.class).getBytes()));

		RmlMapper mapper = RmlMapper.newBuilder()
				.setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
				.build();

		mapper.bindInputStream("stdin",new ByteArrayInputStream(source.getObject(String.class).getBytes()));
		Model m = mapper.map(carmlMapping);
		
		StringWriter outString = new StringWriter();
		Rio.write(m, outString, RDFFormat.NTRIPLES);
		
		return Response.ok(outString.toString()).build();
	}
}
