import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.net.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.events.*;

public class Cep2 extends EventPoster {
	public static void main(String[] args) throws Exception {
		EventPoster ep = new EventPoster();
		for (int i = 0; i < 2; i++) {
			ep.postRandom(ep.sources.get(0), "sasa_cepTestEvent");
			ep.postRandom(ep.sources.get(1), "sasa_cepTestEvent");
			sleep(100L);
		}
	}
}
