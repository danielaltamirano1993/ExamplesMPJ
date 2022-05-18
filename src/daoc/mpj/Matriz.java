package daoc.mpj;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.DoubleStream;

import javax.imageio.ImageIO;

public class Matriz implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String IMG_PATH_01 = "zoom_20x20.png";
	public static final String IMG_PATH_01_B = "zoom_20x20_b.png";
	public static final String IMG_PATH_02 = "zoom_50x50.png";
	public static final String IMG_PATH_02_B = "zoom_50x50_b.png";	
	public static final String IMG_PATH_03 = "zoom_512x512.png";
	
	public static final String IMG_PATH_03_B = "Nuevo.jpg";
	
	//public static final String IMG_PATH_03_B = "zoom_512x512_B.png";
	
	
	
	
	double[][] matriz;
	
	Matriz() {}
	//fs --> filas
	//cs --> columnas
	Matriz(int fs, int cs) {
		matriz = new double[fs][cs];
	}
	Matriz(double[][] matriz) {
		this.matriz = matriz;
	}
	
	double[][] getMatriz() {
		return matriz;
	}
	void setMatriz(double[][] matriz) {
		this.matriz = matriz;
	}
	void setMatriz(double valor) {
		for(int f = 0; f < getFilas(); f++) {
			for(int c = 0; c < getCols(); c++) {
				getMatriz()[f][c] = valor;
			}
		}
	}	
	
	int getFilas() {
		return matriz==null ? 0 : matriz.length;
	}
	int getCols() {
		return matriz==null ? 0 : matriz[0].length;
	}
	
	double getValor(int f, int c) {
		return matriz[f][c];
	}
	void setValor(double valor, int f, int c) {
		matriz[f][c] = valor;
	}		
	
	DoubleStream getAsStream() {
		return Arrays.stream(getMatriz()).
				flatMapToDouble(x -> Arrays.stream(x));
	}
	
	int[] getFrontera(int f, int c) {
		int[] frontera = new int[4];
		int fi, ff, ci, cf;
		fi = f-1 < 0 ? 0 : f-1;
		ff = f+1 < getFilas() ? f+1 : f;
		ci = c-1 < 0 ? 0 : c-1;
		cf = c+1 < getCols() ? c+1 : c;
		frontera[0] = fi;
		frontera[1] = ff;
		frontera[2] = ci;
		frontera[3] = cf;
		return frontera;
	}	

	DoubleStream getVecinos(int f, int c) {
		int[] fr = getFrontera(f, c);
		return Arrays.stream(getMatriz(), fr[0], fr[1]+1).
				flatMapToDouble(x -> Arrays.stream(x, fr[2], fr[3]+1));
	}
	
	Matriz clonar() {
		double[][] copy = Arrays.stream(matriz).map(double[]::clone).toArray(double[][]::new);
		return new Matriz(copy);
	}
	
	Matriz getSubMatriz(int initF, int finF, int initC, int finC) {
		Matriz subM = new Matriz(finF-initF+1, finC-initC+1);
		for(int f = initF; f <= finF; f++) {
			for(int c = initC; c <= finC; c++) {
				subM.setValor(this.getValor(f, c), f-initF, c-initC);
			}
		}
		return subM;
	}
	
	void setSubMatriz(Matriz data, int initF, int finF, int initC, int finC) {
		for(int f = initF; f <= finF; f++) {
			for(int c = initC; c <= finC; c++) {
				this.setValor(data.getValor(f-initF, c-initC), f, c);
			}
		}
	}	
	/**
	 * Devuelve las coordenadas de los límites de cada bloque
	 * bloques debe ser número par. Este algoritmo usa siempre dos columnas para dividir
	 * la matriz. En el arreglo 2d devuelto, la primera dimensión corresponde a cada bloque;
	 * la segunda dimensión: [0]=filaInicial; [1]=filaFinal; [2]=colInicial; [3]=colFinal
	 * @throws Exception 
	 */
	int[][] limitesDivisionBloques(int bloques) {
		if(bloques % 2 != 0) return null;
		int[][] limites = new int[bloques][4];
		int bCols = 2;
		int bFilas = bloques / 2;
		int filas = getFilas() / bFilas;
		int cols = getCols() / bCols;		
		int colAct = 0;
		for(int c = 0; c < bCols; c++) {	
			int ctrlCol = c*bFilas;
			int filaAct = 0;
			for(int f = 0; f < bFilas; f++) {
				limites[ctrlCol+f][2] = colAct;
				limites[ctrlCol+f][0] = filaAct;			
				filaAct = (f==(bFilas-1)) ? getFilas()-1 : filaAct + filas - 1;
				limites[ctrlCol+f][1] = filaAct;
				filaAct += 1;
			}
			colAct = (c==(bCols-1)) ? getCols() - 1 : colAct + cols - 1;			
			for(int f = 0; f < bFilas; f++) {
				limites[ctrlCol+f][3] = colAct;		
			}
			colAct += 1;
		}		
			
		return limites;
	}
	
	/**
	 * Funciona para una matriz original cuadrada, que se pueda subdividir
	 * exactamente en numMat matrices cuadradas de talla bloque.
	 * La matriz se divide fila por fila.
	 * (su contrapartida es reuneBloques)
	 */
	Matriz[] divideEnBloques(int numMat, int bloque) {
		Matriz[] chicas = new Matriz[numMat];
		int counter = 0;
		for(int f = 0; f < numMat; f+=bloque) {
			for(int c = 0; c < numMat; c+=bloque) {
				chicas[counter++] = this.getSubMatriz(f, f+bloque-1, c, c+bloque-1);
			}
		}
		return chicas;
	}
	/**
	 * Funciona si cada chicas es cuadrada, y si forman un grid cuadrado
	 * La matriz resultante se arma fila por fila
	 * (es la contrapartida de divideEnBloques)
	 */
	static Matriz reuneBloques(Matriz[] chicas) {
		int numMat = chicas.length;
		int bloque = chicas[0].getCols();
		int matLado = (int) Math.sqrt(numMat);
		int cellLado = matLado * bloque;
		Matriz nueva = new Matriz(cellLado, cellLado);
		
		int counter = 0;
		for(int f = 0; f < numMat; f+=bloque) {
			for(int c = 0; c < numMat; c+=bloque) {
				nueva.setSubMatriz(chicas[counter++], f, f+bloque-1, c, c+bloque-1);
			}
		}
		
		return nueva;
	}
	
	/**
	 * Carga la imagen en imgFile y le cambia el tamaño a newWidth x newHeight,
	 * Luego crea un arreglo de 3 matrices, una por cada canal de color:
	 * [0] = rojo; [1] = verde; [2] = azul
	 */
	static Matriz[] desdeImagenEscalada(String imgFile, int newWidth, int newHeight) {
		try {
			BufferedImage img = ImageIO.read(new File(imgFile));
		
	        Image tmp = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
	        BufferedImage destimg = new BufferedImage(newWidth, newHeight, img.getType());
	
	        Graphics2D g2d = destimg.createGraphics();
	        g2d.drawImage(tmp, 0, 0, null);
	        g2d.dispose();
	
	        return desdeImagen(destimg);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Carga la imagen en imgFile y crea un arreglo de 3 matrices, una por cada
	 * canal de color: [0] = rojo; [1] = verde; [2] = azul
	 */
	static Matriz[] desdeImagen(String imgFile) {
		try {
			BufferedImage img = ImageIO.read(new File(imgFile));
			return desdeImagen(img);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * A partir de la imagen cargada, crea un arreglo de 3 matrices, una por cada
	 * canal de color: [0] = rojo; [1] = verde; [2] = azul
	 */
	static Matriz[] desdeImagen(BufferedImage img) {
		Matriz[] rgb = new Matriz[3];

        double[][] r = new double[img.getHeight()][img.getWidth()];
        double[][] g = new double[img.getHeight()][img.getWidth()];
        double[][] b = new double[img.getHeight()][img.getWidth()];
        
        for(int row = 0; row < img.getHeight(); row++) {
            for(int col = 0; col < img.getWidth(); col++) {
                Color c = new Color(img.getRGB(col, row));
                r[row][col] = c.getRed();
                g[row][col] = c.getGreen();
                b[row][col] = c.getBlue();
            }
        }
        
        rgb[0] = new Matriz(r);
        rgb[1] = new Matriz(g);
        rgb[2] = new Matriz(b);
        
        return rgb;   			
	}
	/**
	 * Recibe un arreglo de 3 matrices, una por cada canal de color:
	 * [0] = rojo; [1] = verde; [2] = azul
	 * Todas las matrices deben tener exactamente la misma talla.
	 * Crea, a partir de las matrices, una imagen de color y la guarda
	 * en imgFile. El formato será el de la extensión en imgFile
	 */
	static void haciaImagen(Matriz[] rgb, String imgFile) {	
		BufferedImage img = new BufferedImage(rgb[0].getFilas(), rgb[0].getCols(), BufferedImage.TYPE_INT_RGB);
		
        for(int row = 0; row < img.getHeight(); row++) {
            for(int col = 0; col < img.getWidth(); col++) {
                Color c = new Color(
                		(int)rgb[0].getValor(row, col), 
                		(int)rgb[1].getValor(row, col), 
                		(int)rgb[2].getValor(row, col));
                img.setRGB(col, row, c.getRGB());
            }
        }  		
		   
        try {
        	String ext = imgFile.substring(imgFile.length()-3);
        	File outFile = new File(imgFile);
			ImageIO.write(img, ext, outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}        
	}
	
	static Matriz ejemplo01 () {
		double[][] matriz = {
			{1, 2, 3, 10, 20, 30, 100, 200, 300},
			{1, 2, 3, 10, 20, 30, 100, 200, 300},
			{1, 2, 3, 10, 20, 30, 100, 200, 300},
			{4, 5, 6, 40, 50, 60, 400, 500, 600},
			{4, 5, 6, 40, 50, 60, 400, 500, 600},
			{4, 5, 6, 40, 50, 60, 400, 500, 600},
			{7, 8, 9, 70, 80, 90, 700, 800, 900},
			{7, 8, 9, 70, 80, 90, 700, 800, 900},
			{7, 8, 9, 70, 80, 90, 700, 800, 900}
		};
		return new Matriz(matriz);		
	}
	static Matriz ejemplo02 () {
		double[][] matriz = {
			{1, 2, 3, 4, 10, 20, 30, 40},
			{1, 2, 3, 4, 10, 20, 30, 40},
			{1, 2, 3, 4, 10, 20, 30, 40},
			{1, 2, 3, 4, 10, 20, 30, 40},
			{5, 6, 7, 8, 50, 60, 70, 80},
			{5, 6, 7, 8, 50, 60, 70, 80},
			{5, 6, 7, 8, 50, 60, 70, 80},
			{5, 6, 7, 8, 50, 60, 70, 80}
		};
		return new Matriz(matriz);		
	}	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < getFilas(); i++) {
			str.append(Arrays.toString(matriz[i]));
			str.append('\n');
		}
		return str.toString();
	}
	
	public static void main(String[] args) {
//		Matriz[] rgb = Matriz.desdeImagenEscalada(IMG_PATH_01, 60, 60);
//		Matriz.haciaImagen(rgb, IMG_PATH_01_B);
		Matriz uno = Matriz.ejemplo01();
		int[][] limites = uno.limitesDivisionBloques(8);
		System.out.println(uno);
		for(int[] arr : limites)
			System.out.println(Arrays.toString(arr));
//		long cuenta = uno.getVecinos(1, 1).count();
//		double suma = uno.getVecinos(1, 1).sum();
//		double avg = uno.getVecinos(1, 1).average().getAsDouble();
//		System.out.println(cuenta);
//		System.out.println(suma);
//		System.out.println(avg);
//		System.out.println("Listo");
	}
	
}
