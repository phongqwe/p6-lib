// Generated from Cal.g4 by ANTLR 4.9.3
package com.github.xadkile.p6.formula.translator.antlr.eg;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CalLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, ID=5, NUMBER=6, STRING=7, MUL=8, DIV=9,
		ADD=10, SUB=11, MOD=12, EXP=13, NEWLINE=14, WS=15;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "ID", "NUMBER", "DIGIT", "STRING", "ESC",
			"MUL", "DIV", "ADD", "SUB", "MOD", "EXP", "NEWLINE", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "'clear'", "'('", "')'", null, null, null, "'*'", "'/'",
			"'+'", "'-'", "'%'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "ID", "NUMBER", "STRING", "MUL", "DIV",
			"ADD", "SUB", "MOD", "EXP", "NEWLINE", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public CalLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Cal.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\21\177\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\6\6\63\n\6\r\6\16"+
		"\6\64\3\6\7\68\n\6\f\6\16\6;\13\6\3\7\6\7>\n\7\r\7\16\7?\3\7\3\7\7\7D"+
		"\n\7\f\7\16\7G\13\7\3\7\3\7\6\7K\n\7\r\7\16\7L\3\7\6\7P\n\7\r\7\16\7Q"+
		"\5\7T\n\7\3\b\3\b\3\t\3\t\3\t\7\t[\n\t\f\t\16\t^\13\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\n\5\nf\n\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3"+
		"\20\3\21\5\21u\n\21\3\21\3\21\3\22\6\22z\n\22\r\22\16\22{\3\22\3\22\3"+
		"\\\2\23\3\3\5\4\7\5\t\6\13\7\r\b\17\2\21\t\23\2\25\n\27\13\31\f\33\r\35"+
		"\16\37\17!\20#\21\3\2\5\4\2C\\c|\3\2\62;\4\2\13\13\"\"\2\u0089\2\3\3\2"+
		"\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\21"+
		"\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3%\3\2\2\2\5\'\3\2\2\2\7-\3\2"+
		"\2\2\t/\3\2\2\2\13\62\3\2\2\2\rS\3\2\2\2\17U\3\2\2\2\21W\3\2\2\2\23e\3"+
		"\2\2\2\25g\3\2\2\2\27i\3\2\2\2\31k\3\2\2\2\33m\3\2\2\2\35o\3\2\2\2\37"+
		"q\3\2\2\2!t\3\2\2\2#y\3\2\2\2%&\7?\2\2&\4\3\2\2\2\'(\7e\2\2()\7n\2\2)"+
		"*\7g\2\2*+\7c\2\2+,\7t\2\2,\6\3\2\2\2-.\7*\2\2.\b\3\2\2\2/\60\7+\2\2\60"+
		"\n\3\2\2\2\61\63\t\2\2\2\62\61\3\2\2\2\63\64\3\2\2\2\64\62\3\2\2\2\64"+
		"\65\3\2\2\2\659\3\2\2\2\668\5\17\b\2\67\66\3\2\2\28;\3\2\2\29\67\3\2\2"+
		"\29:\3\2\2\2:\f\3\2\2\2;9\3\2\2\2<>\5\17\b\2=<\3\2\2\2>?\3\2\2\2?=\3\2"+
		"\2\2?@\3\2\2\2@A\3\2\2\2AE\7\60\2\2BD\5\17\b\2CB\3\2\2\2DG\3\2\2\2EC\3"+
		"\2\2\2EF\3\2\2\2FT\3\2\2\2GE\3\2\2\2HJ\7\60\2\2IK\5\17\b\2JI\3\2\2\2K"+
		"L\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MT\3\2\2\2NP\5\17\b\2ON\3\2\2\2PQ\3\2\2\2"+
		"QO\3\2\2\2QR\3\2\2\2RT\3\2\2\2S=\3\2\2\2SH\3\2\2\2SO\3\2\2\2T\16\3\2\2"+
		"\2UV\t\3\2\2V\20\3\2\2\2W\\\7$\2\2X[\5\23\n\2Y[\13\2\2\2ZX\3\2\2\2ZY\3"+
		"\2\2\2[^\3\2\2\2\\]\3\2\2\2\\Z\3\2\2\2]_\3\2\2\2^\\\3\2\2\2_`\7$\2\2`"+
		"\22\3\2\2\2ab\7^\2\2bf\7$\2\2cd\7^\2\2df\7^\2\2ea\3\2\2\2ec\3\2\2\2f\24"+
		"\3\2\2\2gh\7,\2\2h\26\3\2\2\2ij\7\61\2\2j\30\3\2\2\2kl\7-\2\2l\32\3\2"+
		"\2\2mn\7/\2\2n\34\3\2\2\2op\7\'\2\2p\36\3\2\2\2qr\7`\2\2r \3\2\2\2su\7"+
		"\17\2\2ts\3\2\2\2tu\3\2\2\2uv\3\2\2\2vw\7\f\2\2w\"\3\2\2\2xz\t\4\2\2y"+
		"x\3\2\2\2z{\3\2\2\2{y\3\2\2\2{|\3\2\2\2|}\3\2\2\2}~\b\22\2\2~$\3\2\2\2"+
		"\17\2\649?ELQSZ\\et{\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
