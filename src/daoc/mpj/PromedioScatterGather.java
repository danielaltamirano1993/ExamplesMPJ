package daoc.mpj;

import java.util.Random;

import mpi.MPI;

public class PromedioScatterGather {

	public static void main(String[] args) {
		final int master = 0;
		Random rnd = new Random(); 
		int cuantos = 20000000;
		int[] numeros = new int[cuantos];
		
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		
		//el master genera números aleatorios
		if(rank == master) {
			for(int i = 0; i < cuantos; i++) {
				numeros[i] = rnd.nextInt();
			}
			// solo para confirmar
			System.out.println("Promedio de control: " + promedio(numeros));			
		}
		
		// se reparten los números
		int[] misnumeros = new int[cuantos/size];
		MPI.COMM_WORLD.Scatter(numeros, 0, cuantos/size, MPI.INT, misnumeros, 0, cuantos/size, MPI.INT, master);
		
		// cada uno calcula su promedio
		double[] mipromedio = {promedio(misnumeros)};
		System.out.println(String.format("Mi promedio( rank: %d): %f", rank, mipromedio[0]));
		
		// se recuperan los promedios parciales
		double[] lospromedios = new double[size];		
		MPI.COMM_WORLD.Gather(mipromedio, 0, 1, MPI.DOUBLE, lospromedios, 0, 1, MPI.DOUBLE, master);
	
		// el master calcula el promedio final
		if(rank == master) {
			System.out.println("Promedio global: " + promedio(lospromedios));
		}
		
		MPI.Finalize();
	}

	private static double promedio(int[] numeros) {
		double promedio = 0;
		for(int i = 0; i < numeros.length; i++) {
			promedio += numeros[i];
		}
		return promedio /= numeros.length;
	}
	private static double promedio(double[] numeros) {
		double promedio = 0;
		for(int i = 0; i < numeros.length; i++) {
			promedio += numeros[i];
		}
		return promedio /= numeros.length;
	}	
}
