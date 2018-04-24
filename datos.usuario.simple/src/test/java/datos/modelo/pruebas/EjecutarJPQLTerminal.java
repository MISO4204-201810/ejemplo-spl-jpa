package datos.modelo.pruebas;

import cc.jpa.JPQLe;

public class EjecutarJPQLTerminal {

	
	public static void main(String[] args) {
		
		DatosPrueba.borrarDatosPrueba();
		DatosPrueba.crearDatosPrueba();
		
		JPQLe.main( new String[]{"test_PU"} );
		
	}
	
}
