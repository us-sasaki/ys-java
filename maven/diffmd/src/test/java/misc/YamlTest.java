package misc;

import java.io.*;

import org.yaml.snakeyaml.Yaml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class YamlTest extends TestCase {
	
	public void testYaml() {
		try {
			Reader r = new FileReader("diffmd.yaml");
			
			Yaml y = new Yaml();
			DiffMdProps p = y.loadAs(r, DiffMdProps.class);
			System.out.println("Yaml---------------------------");
			System.out.println(p);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}

