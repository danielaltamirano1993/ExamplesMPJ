package daoc.mpj;

import mpi.MPI;

public class MatrizBloques {

	/**
	 * Este ejemplo está armado para una matriz original de 9x9 que se divide
	 * en 9 submatrices de 3x3
	 * Requiere que haya 9 procesos (MPI.COMM_WORLD.Size)
	 * Podría funcionar para matrices cuadradas que puedan subdividirse en un 
	 * número exacto de submatrices cuadradas. Debe haber un número de
	 * procesos igual al número de submatrices
	 */
	public static void main(String[] args) {
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		
		int master = 0;
		int numMat = 9;
		int bloque = 3;
		Matriz[] chicas = new Matriz[numMat];
		Matriz[] mLocal = new Matriz[1];
		
		if(rank == master ) {
			Matriz grande = Matriz.ejemplo01();
			System.out.println(grande);
			System.out.println("==========");
			chicas = grande.divideEnBloques(numMat, bloque);
			for(Matriz m : chicas) System.out.println(m);
			System.out.println("==========");
		}
		
		MPI.COMM_WORLD.Scatter(chicas, 0, 1, MPI.OBJECT, mLocal, 0, 1, MPI.OBJECT, master);
		
		//sacamos el promedio de todos los valores en la (sub)matriz recibida
		double promedio = mLocal[0].getAsStream().average().getAsDouble();
		
		//reemplazamos todos los valores de la (sub)matriz, con el promedio calculado
		mLocal[0].setMatriz(promedio);	
		
		Matriz[] lasmatrices = new Matriz[numMat];		
		MPI.COMM_WORLD.Gather(mLocal, 0, 1, MPI.OBJECT, lasmatrices, 0, 1, MPI.OBJECT, master);
		
		if(rank == master ) {
			for(Matriz m : lasmatrices) System.out.println(m);
			System.out.println("==========");
			Matriz nueva = Matriz.reuneBloques(lasmatrices);
			System.out.println(nueva);
		}

		MPI.Finalize();
	}

}
