import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.net.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.events.*;

public class Cep1 extends EventPoster {
	public static void main(String[] args) throws Exception {
		EventPoster ep = new EventPoster();
		ep.postRandom(ep.sources.get(0), "sasa_cepTestEvent");
		ep.postRandom(ep.sources.get(1), "sasa_cepTestEvent");
	}
}
