package es.gob.fire.signature;

/**
 * Estado de una aplicaci&oacute;n.
 */
public class ApplicationChecking {

	private final String id;
	private final String name;
	private final boolean	valid;

	/**
	 * Identifica el estado de una aplicaci&oacute;n.
	 * @param id Identificador de la aplicaci&oacute;n.
	 * @param name Nombre de la aplicaci&oacute;n.
	 * @param valid Indica si es v&aacute;lida ({@code true}) o no ({@code false}).
	 */
	public ApplicationChecking(final String id, final String name, final boolean valid) {
		this.id = id;
		this.name = name;
		this.valid = valid;
	}

	/**
	 * Identificador de la aplicaci&oacute;n.
	 * @return Identificador de la aplicaci&oacute;n.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Nombre de la aplicaci&oacute;n.
	 * @return Nombre de la aplicaci&oacute;n.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Indica si la aplicaci&oacute;n es v&aacute;lida o no.
	 * @return {@code true} si la aplicaci&oacute;n es v&aacute;lida y si el sistema
	 * permite que se le realicen peticiones, {@code false} en caso contrario.
	 */
	public boolean isValid() {
		return this.valid;
	}
}
