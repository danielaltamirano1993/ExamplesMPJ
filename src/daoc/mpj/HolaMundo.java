package daoc.mpj;

import mpi.MPI;

public class HolaMundo {

	public static void main(String[] args) {
		MPI.Init(args);
		
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		
		System.out.println("Hola # " + rank + " de " + size);
		
		MPI.Finalize();
	}

}
