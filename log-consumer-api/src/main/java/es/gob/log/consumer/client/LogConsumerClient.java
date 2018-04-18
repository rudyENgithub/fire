package es.gob.log.consumer.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.client.HttpManager.UrlHttpMethod;

/**
 * Cliente para la consulta de logs.
 */
public class LogConsumerClient {

	private static final Logger LOGGER = Logger.getLogger(LogConsumerClient.class.getName());

	private String serviceUrl = null;

	private final HttpManager conn;

	/**
	 * Construye el cliente para la consulta de logs.
	 */
	public LogConsumerClient() {
		this.conn = new HttpManager();
	}

	/**
	 * Inicializa el cliente para la conexi&oacute;n con un servicio de consulta
	 * de logs.
	 * @param url URL del servicio de consulta de logs.
	 * @param keyB64 Clave de inicio de sesion en base 64.
	 * @throws IOException
	 */
	public void init(final String url, final String keyB64) throws IOException {

		if (url == null) {
			throw new NullPointerException("La url del servicio no puede ser nula"); //$NON-NLS-1$
		}

		if (keyB64 == null) {
			throw new NullPointerException("La clave de inicio de sesion no puede ser nula"); //$NON-NLS-1$
		}

		if (this.serviceUrl != null) {
			try {
				closeFile();
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "No se pudo cerrar el fichero abierto", e); //$NON-NLS-1$
			}
		}

		this.serviceUrl = url;



		// Solicitamos login
		StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.REQUEST_LOGIN.ordinal()); //$NON-NLS-1$

		HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

		// Procesamos la respuesta
		byte[] token = null;
		byte[] iv = null;

		try (final JsonReader reader = Json.createReader(
				new ByteArrayInputStream(response.getContent()));) {
			final JsonObject loginReponse = reader.readObject();
			final String tokenEncoded = loginReponse.getString(ResponseParams.PARAM_TOKEN);
			if (tokenEncoded != null) {
				token = Base64.decode(tokenEncoded);
			}
			final String ivEncoded = loginReponse.getString(ResponseParams.PARAM_IV);
			if (ivEncoded != null) {
				iv = Base64.decode(ivEncoded);
			}
		}

		if (token == null || iv == null) {
			throw new IOException("No se ha obtenido el token de autenticacion y la configuracion necesaria"); //$NON-NLS-1$
		}

		// Procesamos el token
		final byte[] cipherKey = Base64.decode(keyB64);
		byte[] cipheredToken;
		try {
			cipheredToken = Cipherer.cipher(token, cipherKey, iv);
		} catch (final GeneralSecurityException e) {
			throw new IOException("No se puso negociar la sesion con el servidor", e); //$NON-NLS-1$
		}

		// Solicitamos validacion de login
		 urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.VALIDATE_LOGIN.ordinal()) //$NON-NLS-1$
					.append("&sc=").append(Base64.encode(cipheredToken, true)); //$NON-NLS-1$

		 response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.POST);

		// Procesamos la respuesta
		 boolean logged = false;
		 try (final JsonReader reader = Json.createReader(
					new ByteArrayInputStream(response.getContent()));) {
				final JsonObject loginReponse = reader.readObject();
				logged = loginReponse.getBoolean(ResponseParams.PARAM_RESULT);
			}
		 //TODO borrar solo para pruebas
		 logged = true;
		 //fin TODO
		 if (!logged) {
			 throw new IOException("El servidor nego el acceso al usuario"); //$NON-NLS-1$
		 }
	}

	/**
	 * Permite desactivar las comprobaciones que se realizan autom&aacute;ticamente
	 * sobre el certificado SSL servidor.
	 * @param disable {@code true} para desactitivar las comprobaciones de seguridad,
	 * {@code false} en caso contrario.
	 */
	public void setDisableSslChecks(final boolean disable) {
		this.conn.setDisabledSslChecks(disable);
	}

	/**
	 * Indica si se encuentran deshabilitadas las comprobaciones autom&aacute;aticas
	 * sobre los certificados SSL servidor.
	 * @return {@code true} si las comprobaciones de seguridad se encuentran desactivadas,
	 * {@code false} en caso contrario.
	 */
	public boolean isDisabledSslChecks() {
		return this.conn.isDisabledSslChecks();
	}

	/**
	 * Consulta la disponibilidad de un servicio de consulta de logs.
	 * @param serverUrl URL del servicio.
	 * @return Respuesta del servicio al saludo.
	 * @throws IOException Cuando no es posible contactar con el servicio.
	 */
	public String echo(final String serverUrl) throws IOException {

		if (serverUrl == null) {
			throw new NullPointerException("No se ha indicado la URL de redireccion"); //$NON-NLS-1$
		}

		final StringBuilder requestUrl = new StringBuilder(serverUrl)
				.append("?op=").append(ServiceOperations.ECHO.ordinal()); //$NON-NLS-1$

		HttpResponse result;
		try {
			result = this.conn.readUrl(requestUrl.toString(), HttpManager.UrlHttpMethod.GET);
		}
		catch (final IOException e) {
			throw new IOException("No se ha podido conectar con el servicio indicado", e);
		}

		final byte[] response = result.getContent();
		if (response == null) {
			throw new IOException("El servicio no respondio a la peticion realizada"); //$NON-NLS-1$
		}


		return new String(response);
	}

	public String[] getLogFiles() {

		try {

			final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.GET_LOG_FILES.ordinal() ); //$NON-NLS-1$
			final HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			//response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
			final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));
			final JsonObject listFilesReponse = reader.readObject();



		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public byte[] openFile(final String filename) {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public byte[] closeFile() {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public static byte[] getLogTail(final int numLines) {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public byte[] getMoreLog(final int numLines) {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public byte[] getLogFiltered(final int numLines, final Criteria criteria) {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public byte[] searchText(final int numLines, final String text, final Date startDate) {
		throw new UnsupportedOperationException("Metodo no implementado");
	}

	public byte[] download() {
		throw new UnsupportedOperationException("Metodo no implementado");
	}
}
