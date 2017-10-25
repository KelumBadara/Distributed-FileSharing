import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Demo {
	public static void main(String args[]) {
		ArrayList<String> files = new ArrayList<String>();
		
		
		try {
			FileInputStream fstream = new FileInputStream("E:/fileNames.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			
			while ((line = br.readLine()) != null) {
		       files.add(line);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ArrayList<Node> nodes= new ArrayList<Node>();
		
		for(int j=0; j<10; j++){
			ArrayList<String> fileList = new ArrayList<String>();
			for(int i = 0; i<3; i++){
				Random random = new Random();
				int rand = random.nextInt(10);
				fileList.add(files.get(rand));
			}
			Node node = new Node("localhost", 6500+j, "Aztec"+j, fileList, 55555, "localhost");	
			nodes.add(node);
		}
		
		nodes.get(0).search("Windows XP");
		
	}

}
