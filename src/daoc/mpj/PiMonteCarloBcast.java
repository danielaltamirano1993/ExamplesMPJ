package daoc.mpj;

import java.util.Random;

import mpi.MPI;

// Usa Bcast
public class PiMonteCarloBcast {

	public static void main(String[] args) {
		long[] adentro = {0};
		final Random rnd = new Random();
		final int master = 0;
		final int tag = 1;
		
		MPI.Init(args);
		final int rank = MPI.COMM_WORLD.Rank();
		final int size = MPI.COMM_WORLD.Size();

		System.out.println(String.format("Listo %d de %d", rank, size));
		
		long[] mispuntos = {0};
		if(rank == master) {
			mispuntos[0] = (long) (10000000 * rnd.nextDouble());
			System.out.println(mispuntos[0]);
		}
		MPI.COMM_WORLD.Bcast(mispuntos, 0, 1, MPI.LONG, master);

		//todos calculan su parte
		for(int i = 0; i < mispuntos[0]; i++) {
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
			double pi = 4 * ((double)adentro[0] / (mispuntos[0] * size));
			System.out.println(String.format("Pi aprox.: %f con %d puntos", pi, mispuntos[0] * size));
		} else {//slaves envían su cálculo
			MPI.COMM_WORLD.Send(adentro, 0, 1, MPI.LONG, master, tag);
		}
		
		MPI.Finalize();		
	}

}
