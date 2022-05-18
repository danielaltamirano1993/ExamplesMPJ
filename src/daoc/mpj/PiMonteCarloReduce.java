package daoc.mpj;

import java.util.Random;

import mpi.MPI;

public class PiMonteCarloReduce {

	public static void main(String[] args) {
		final long puntos = 200000000;
		long[] adentro = {0};
		final Random rnd = new Random();
		final int master = 0;
		
		MPI.Init(args);
		final int rank = MPI.COMM_WORLD.Rank();
		final int size = MPI.COMM_WORLD.Size();

		System.out.println(String.format("Listo %d de %d", rank, size));

		//todos calculan su parte
		for(int i = 0; i < (puntos/size); i++) {
			double x = rnd.nextDouble();
			double y = rnd.nextDouble();
			if( (x*x + y*y) <= 1 ) {
				adentro[0]++;
			}
		}
		
		long[] todosadentro = {0};
		MPI.COMM_WORLD.Reduce(adentro, 0, todosadentro, 0, 1, MPI.LONG, MPI.SUM, master);
		
		if(rank == master) {//master hace cálculo final
			double pi = 4 * ((double)todosadentro[0] / puntos);
			System.out.println(String.format("Pi aprox.: %f con %d puntos", pi, puntos));
		}
		
		MPI.Finalize();		
	}

}
