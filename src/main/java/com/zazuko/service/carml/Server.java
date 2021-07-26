package com.zazuko.service.carml;

import org.apache.meecrowave.Meecrowave;

public class Server {

	public static void main(String[] args) {
		final Meecrowave.Builder builder = new Meecrowave.Builder();
		builder.setScanningPackageIncludes("com.zazuko.service.carml");
		builder.setJaxrsMapping("/api/*");
		
		try (Meecrowave meecrowave = new Meecrowave(builder)) {
			meecrowave.bake().await();
		}
	}

}
