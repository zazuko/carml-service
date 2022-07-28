package com.zazuko.service.carml;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.logicalsourceresolver.XPathResolver;
import io.carml.logicalsourceresolver.JsonPathResolver;
import io.carml.logicalsourceresolver.CsvResolver;
import io.carml.model.TriplesMap;
import io.carml.util.RmlMappingLoader;
import io.carml.vocab.Rdf;


public class CarmlEndpointCore {

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

		RdfRmlMapper mapper = RdfRmlMapper.builder()
				.triplesMaps(carmlMapping)
				.setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance)
			    .setLogicalSourceResolver(Rdf.Ql.XPath, XPathResolver::getInstance)
			    .setLogicalSourceResolver(Rdf.Ql.Csv, CsvResolver::getInstance) 
				.build();
		
		
		outputFormat = Rio.getParserFormatForMIMEType(targetType).orElse(RDFFormat.NTRIPLES);
		try {
			Model m = mapper.mapToModel(Map.of("stdin",new ByteArrayInputStream(source.getObject(String.class).getBytes())));

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
