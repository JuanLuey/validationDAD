package pe.intercorp.ValidateDad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.stream.IntStream;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import pe.intercorp.ValidateDad.entity.Product;
import pe.intercorp.ValidateDad.entity.Sku;
import pe.intercorp.ValidateDad.repository.RepoApi;
import pe.intercorp.ValidateDad.repository.RepoData;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class ValidateDadApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ValidateDadApplication.class, args);
	}

	@Autowired
	RepoApi repoApi;

	@Autowired
	RepoData repoData;

	private final Logger log = LoggerFactory.getLogger(ValidateDadApplication.class);
	int intentos = 0;

	@SuppressWarnings("unchecked")
	public void run(String... args) throws Exception {

		String v_Token = repoApi.loginDAD();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		List<Sku> skus = repoData.getSkus();

		// for (int i = 0; i < skus.size(); i++) {

		if (skus.size() == 0) {
			log.info("No existen registros a procesar {}", skus.size());
		}

		IntStream.range(0, skus.size())
				.parallel()
				.forEach(i -> {
					try {
						Thread.sleep(2);

						String status = "OK";

						String sku = skus.get(i).getSku();
						String marca = skus.get(i).getMarca();
						String codbar = skus.get(i).getCodbar();

						if (marca == null) {
							marca = "";
						}
						if (codbar == null) {
							codbar = "";
						}

						String returnDad = repoApi.getProductDAD(sku, v_Token);

						if (returnDad == "401") {
							log.info("Error em Api, volver a conectar {}", returnDad);
							intentos = intentos + 1;
							if (intentos <= 3) {
								SpringApplication.run(ValidateDadApplication.class, args);
							} else {
								log.info("Error em Api, sobrepaso nro de intentos conexión {}", returnDad);
							}
						}

						List<Product> products = null;

						if (returnDad.equalsIgnoreCase("[]")) {
							status = "Not exists";
						} else {

							products = objectMapper.readValue(returnDad, new TypeReference<List<Product>>() {
							});
							if (products.get(0).getBarcodes() == null) {
								if (codbar.equals("0")) {

								} else {
									status = "Not barcode";
								}
							}

							if (products.get(0).getAttributes() == null) {
								status = "Not attributes";
							} else {
								var attribeName = "";
								attribeName = "NOMBRE DE MARCA";
								Map<String, String> attribs = (Map<String, String>) products.get(0).getAttributes();
								if (attribs.get(attribeName) == null) {
									if (!marca.equals("")) {
										log.info("Not attributes : {}", attribeName);
										status = "Not attributes " + attribeName;
									}
								}
							}
						}

						if (status == "OK") {
							if (codbar.equals("")) {
								codbar = "0";
							}
							repoData.insert_sku_validate(sku, marca, codbar);
							log.info("Insert SKU STG : OK {}", sku);
						} else {
							repoData.insert_sku_stg(sku, marca, codbar);
							log.info("Insert SKU STG : Enviar archivo al DAD {}", sku + " - " + status);
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

		// }
		List<Sku> creationArch = repoData.getCreationArch();
		if (creationArch.size() != 0) {
			log.info("Ini creacion de archivos en FTP ...");
			repoData.process_files_dad();
			log.info("Fin creacion de archivos en FTP ...");

			repoData.update_sku_arch();
			log.info("Fin proceso ...");
		}
	}

}
