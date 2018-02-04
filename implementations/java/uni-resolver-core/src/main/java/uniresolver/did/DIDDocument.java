package uniresolver.did;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class DIDDocument {

	public static final String JSONLD_TERM_ID = "id";
	public static final String JSONLD_TERM_TYPE = "type";
	public static final String JSONLD_TERM_SERVICE = "service";
	public static final String JSONLD_TERM_SERVICEENDPOINT = "serviceEndpoint";
	public static final String JSONLD_TERM_PUBLICKEY = "publicKey";
	public static final String JSONLD_TERM_PUBLICKEYBASE64 = "publicKeyBase64";
	public static final String JSONLD_TERM_PUBLICKEYHEX = "publicKeyHex";

	public static final Object JSONLD_CONTEXT;

	private final Map<String, Object> jsonLdObject;

	static {

		try {

			JSONLD_CONTEXT = JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("diddocument-context.jsonld"));
		} catch (IOException ex) {

			throw new ExceptionInInitializerError(ex);
		}
	}

	private DIDDocument(Map<String, Object> jsonLdObject) {

		this.jsonLdObject = jsonLdObject;
	}

	/*
	 * Factory methods
	 */

	public static DIDDocument build(Map<String, Object> jsonLdObject) {

		return new DIDDocument(jsonLdObject);
	}

	public static DIDDocument build(String id, List<PublicKey> publicKeys, List<Service> services) {

		// add 'id'

		Map<String, Object> jsonLdObject = new LinkedHashMap<String, Object> ();
		jsonLdObject.put(JSONLD_TERM_ID, id);

		// add 'publicKey'

		if (publicKeys != null) {

			LinkedList<Object> publicKeysJsonLdArray = new LinkedList<Object> ();

			for (PublicKey publicKey : publicKeys) {

				Map<String, Object> publicKeyJsonLdObject = publicKey.getJsonLdObject();

				publicKeysJsonLdArray.add(publicKeyJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_PUBLICKEY, publicKeysJsonLdArray);
		}

		// add 'service'

		if (services != null) {

			LinkedList<Object> servicesJsonLdArray = new LinkedList<Object> ();

			for (Service service : services) {

				Map<String, Object> serviceJsonLdObject = service.getJsonLdObject();

				servicesJsonLdArray.add(serviceJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_SERVICE, servicesJsonLdArray);
		}

		// done

		return new DIDDocument(jsonLdObject);
	}

	/*
	 * Serialization
	 */

	@SuppressWarnings("unchecked")
	public static DIDDocument fromJson(String jsonString) throws IOException {

		Map<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(jsonString);

		return build(jsonLdObject);
	}

	public static DIDDocument fromJson(InputStream input, String enc) throws IOException {

		return fromJson(IOUtils.toString(input, StandardCharsets.UTF_8));
	}

	public static DIDDocument fromJson(Reader reader) throws IOException {

		return fromJson(IOUtils.toString(reader));
	}

	@SuppressWarnings("unchecked")
	@JsonRawValue
	public String toJson() throws IOException, JsonLdError {

		Map<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("diddocument-skeleton.jsonld"));
		jsonLdObject.putAll(this.jsonLdObject);

		JsonLdOptions options = new JsonLdOptions();
		Object rdf = JsonLdProcessor.compact(jsonLdObject, JSONLD_CONTEXT, options);
		String result = JsonUtils.toPrettyString(rdf);

		return result;
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getJsonLdObject() {

		return this.jsonLdObject;
	}

	public void setJsonLdObjectKeyValue(String key, Object value) {

		this.jsonLdObject.put(key, value);
	}

	public String getId() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_ID);
		if (entry == null) return null;
		if (! (entry instanceof URI)) return null;

		String id = (String) entry;

		return id;
	}

	@SuppressWarnings("unchecked")
	public List<PublicKey> getPublicKeys() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_PUBLICKEY);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> publicKeysJsonLdArray = (LinkedList<Object>) entry;

		List<PublicKey> publicKeys = new ArrayList<PublicKey> ();

		for (Object entry2 : publicKeysJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> publicKeyJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			publicKeys.add(PublicKey.build(publicKeyJsonLdObject));
		}

		return publicKeys;
	}

	@SuppressWarnings("unchecked")
	public List<Service> getServices() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_SERVICE);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> servicesJsonLdArray = (LinkedList<Object>) entry;

		List<Service> controls = new ArrayList<Service> ();

		for (Object entry2 : servicesJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			Map<String, Object> serviceJsonLdObject = (Map<String, Object>) entry2;

			controls.add(Service.build(serviceJsonLdObject));
		}

		return controls;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		try {

			return this.toJson();
		} catch (IOException | JsonLdError ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
