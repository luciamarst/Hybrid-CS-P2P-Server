package monitores;

public enum TipoSincronizacion {
	INCREMENTO_DECREMENTO(0),
    PRODUCTOR_CONSUMIDOR(1),
    LECTOR_ESCRITOR(2);

    private final int codigo;

    TipoSincronizacion(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
