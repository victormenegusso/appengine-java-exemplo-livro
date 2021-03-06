package estudo.appengine.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.logging.Logger;
import estudo.appengine.models.Product;

@Path("/products")
public class ProductManager {

	private static final Logger log = Logger.getLogger(ProductManager.class.getName());
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Product saveProduct(Product product) {
		log.fine("Iniciando salve de produto");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key productKey = KeyFactory.createKey("Products", "productKey");
		Entity productEntity = new Entity("Products", productKey);
		productToEntity(product, productEntity);
		datastore.put(productEntity);
		product.setId(productEntity.getKey().getId());
		log.info("Produto com código=[" + product.getId() + "] salvo com sucesso");
		return product;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Product> getProducts() {
		log.fine("Iniciando listagem de produtos");
		List<Product> products = new ArrayList<>();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query;
		query = new Query("Products").addSort("Code", SortDirection.ASCENDING);
		List<Entity> productsEntities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		for (Entity productEntity : productsEntities) {
			Product product = entityToProduct(productEntity);
			products.add(product);
		}
		return products;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{code}")
	public Product getProduct(@PathParam("code") int code) {
		log.fine("Iniciando get no produto com código=[" + code +"]");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codeFilter = new FilterPredicate("Code", FilterOperator.EQUAL, code);
		Query query = new Query("Products").setFilter(codeFilter);

		Entity productEntity = datastore.prepare(query).asSingleEntity();
		if (productEntity != null) {
			Product product = entityToProduct(productEntity);
			log.info("Produto com código=[" + code + "] encontrado com sucesso");
			return product;
		} else {
			log.severe ("Erro ao encontrar produto com código=[" + code + "]. Produto não encontrado!!!");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{code}")
	public Product alterProduct(@PathParam("code") int code, Product product) {
		log.fine("Iniciando alteracao no produto com código=[" + code +"]");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codeFilter = new FilterPredicate("Code", FilterOperator.EQUAL, code);
		Query query = new Query("Products").setFilter(codeFilter);
		Entity productEntity = datastore.prepare(query).asSingleEntity();
		if (productEntity != null) {
			productToEntity(product, productEntity);
			datastore.put(productEntity);
			product.setId(productEntity.getKey().getId());
			log.info("Produto com código=[" + code + "] altarado com sucesso");
			return product;
		} else {
			log.severe ("Erro ao alterar produto com código=[" + code + "]. Produto não encontrado!!!");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{code}")
	public Product deleteProduct(@PathParam("code") int code) {
		log.fine("Tentando apagar produto com código=[" + code +"]");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codeFilter = new FilterPredicate("Code", FilterOperator.EQUAL, code);
		Query query = new Query("Products").setFilter(codeFilter);
		Entity productEntity = datastore.prepare(query).asSingleEntity();
		if (productEntity != null) {
			datastore.delete(productEntity.getKey());
			Product product = entityToProduct(productEntity);
			log.info("Produto com código=[" + code + "] apagado com sucesso");
			return product;
		} else {
			log.severe ("Erro ao apagar produto com código=[" + code + "]. Produto não encontrado!!!");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	private void productToEntity(Product product, Entity productEntity) {
		productEntity.setProperty("ProductID", product.getProductID());
		productEntity.setProperty("Name", product.getName());
		productEntity.setProperty("Code", product.getCode());
		productEntity.setProperty("Model", product.getModel());
		productEntity.setProperty("Price", product.getPrice());
	}

	private Product entityToProduct(Entity productEntity) {
		Product product = new Product();
		product.setId(productEntity.getKey().getId());
		product.setProductID((String) productEntity.getProperty("ProductID"));
		product.setName((String) productEntity.getProperty("Name"));
		product.setCode(Integer.parseInt(productEntity.getProperty("Code").toString()));
		product.setModel((String) productEntity.getProperty("Model"));
		product.setPrice(Float.parseFloat(productEntity.getProperty("Price").toString()));
		return product;
	}
}
