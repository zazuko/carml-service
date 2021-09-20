package com.zazuko.service.carml;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.eclipse.rdf4j.common.lang.FileFormat;
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
	public Response doConversion(@HeaderParam("Accept") String targetType,
			@Multipart(value = "mapping") Attachment mapping, @Multipart(value = "source") Attachment source) {
		StringWriter outString = new StringWriter();
		RDFFormat outputFormat;
		Set<TriplesMap> carmlMapping = RmlMappingLoader.build().load(
				Rio.getParserFormatForMIMEType(mapping.getContentType().toString()).orElse(RDFFormat.TURTLE),
				new ByteArrayInputStream(mapping.getObject(String.class).getBytes()));

		RmlMapper mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver()).build();

		mapper.bindInputStream("stdin", new ByteArrayInputStream(source.getObject(String.class).getBytes()));

		outputFormat = Rio.getParserFormatForMIMEType(targetType).orElse(RDFFormat.NTRIPLES);
		try {
			Model m = mapper.map(carmlMapping);

			Rio.write(m, outString, outputFormat);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			StringWriter ow = new StringWriter();
			JsonWriter jw = Json.createWriter(ow);

			JsonObject errorObject = Json.createObjectBuilder().add("title", e.getMessage())
					.add("detail", sw.toString()).build();
			jw.writeObject(errorObject);
			jw.close();

			return Response.status(Response.Status.BAD_REQUEST).entity(ow.toString()).type("application/problem+json")
					.build();
		}
		return Response.ok(outString.toString()).type(outputFormat.getDefaultMIMEType()).build();
	}
}
