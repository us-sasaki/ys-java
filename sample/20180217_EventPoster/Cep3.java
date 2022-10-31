import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.net.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.events.*;

public class Cep3 extends EventPoster {
	public static void main(String[] args) throws Exception {
		EventPoster ep = new EventPoster();
		while (true) {
			for (int i = 0; i < 50; i++) {
				ep.postValue(ep.sources.get(0), "sasa_cepTestEvent", i);
				ep.postValue(ep.sources.get(1), "sasa_cepTestEvent", i+50);
				sleep(1000L);
			}
		}
	}
}
