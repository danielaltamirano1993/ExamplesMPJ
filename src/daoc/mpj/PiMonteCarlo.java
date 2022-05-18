package daoc.mpj;

import java.util.Random;

import mpi.MPI;

public class PiMonteCarlo {

	public static void main(String[] args) {
		final long puntos = 200000000;
		long[] adentro = {0};
		final Random rnd = new Random();
		final int master = 0;
		final int tag = 1;
		
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
		
		if(rank == master) {//master recolecta todo y hace cálculo final
			long[] tmp_adentro = {0};
			for(int i = 1; i < size; i++) {
				MPI.COMM_WORLD.Recv(tmp_adentro, 0, 1, MPI.LONG, i, tag);
				adentro[0] += tmp_adentro[0];
			}
			double pi = 4 * ((double)adentro[0] / puntos);
			System.out.println(String.format("Pi aprox.: %f con %d puntos", pi, puntos));
		} else {//slaves envían su cálculo
			MPI.COMM_WORLD.Send(adentro, 0, 1, MPI.LONG, master, tag);
		}
		
		MPI.Finalize();		
	}

}
