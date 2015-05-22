package assembly;

import java.util.Map;

import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class Assembler {
	private Parser parser;
	private Tokenizer tokenizer;
	
	public Parser getParser() {
		return parser;
	}
	public void setParser(Parser parser) {
		this.parser = parser;
	}
	public Tokenizer getTokenizer() {
		return tokenizer;
	}
	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	public Map<String, Object> assemble() {
		
	}
	
}
