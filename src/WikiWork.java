import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.RelationType;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionaryEntryFilter;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalCase;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalDegree;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalNumber;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalPerson;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalTense;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import de.tudarmstadt.ukp.jwktl.api.util.NonFiniteForm;


public class WikiWork {
	public static void doParse() {
		File dumpFile = new File("/home/canwang/downey/old_data/current.xml");
		//File dumpFile = new File("/home/canwang/downey/data/cur.xml");
		File outputDir = new File("/home/canwang/downey/data/output/");
		boolean overwriteExisting = true;
		
		JWKTL.parseWiktionaryDump(dumpFile, outputDir, overwriteExisting);
	}
	public static String getVerbID(GrammaticalPerson person, GrammaticalTense tense, NonFiniteForm nff) {
		if(person != null && person == GrammaticalPerson.THIRD)
			return "ID_THIRD_PERSON_SINGULAR_SIMPLE_PRESENT";
		if(tense != null) {
			if(tense == GrammaticalTense.PRESENT)
				return "ID_PRESENT_PARTICIPLE";
			else if(tense == GrammaticalTense.PAST && nff != null && nff == NonFiniteForm.PARTICIPLE)
				return "ID_PAST_PARTCIPLE";
			else
				return "ID_SIMPLE_PAST";
		}
		return "ERROR";
	}
	public static String getNounID(GrammaticalNumber number) {
		if(number != null && number == GrammaticalNumber.PLURAL)
			return "ID_PLURAL";
		return "ID_ROOT";
	}
	public static String getAdjOrAdvID(GrammaticalDegree degree) {
		if(degree == GrammaticalDegree.COMPARATIVE)
			return "ID_COMPARATIVE";
		else if(degree == GrammaticalDegree.SUPERLATIVE)
			return "ID_SUPERLATIVE";
		return "ERROR";
	}
	public static boolean isVowel(char ch) {
		return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
	}
	public static void main(String[] args) {
		doParse();
		//Set<String> wordDict = new HashSet<String>();
		doProc();
	}
	
	public static void insertEntry(OutputStream os, String str, Set<String> entrySet) {
		if(!entrySet.contains(str)) {
			try {
				//System.out.println(str);
				os.write((str+"\n").getBytes());
				entrySet.add(str);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static
	String [] SUFFIXS = {"er", "ly", "able", "ible", "hood", "ful", "less", "ish", "ness", "ic", "ist",
						 "ian", "or", "eer", "logy", "ship", "ous", "ive", "age", "ant", "ent", "ment",
						 "ary", "ize", "ise", "ure", "ion", "ation", "ance", "ence", "ity", "al", "tial",
						 "cial", "ate", "tude", "ism"};
	static
	String [] PREFIXS = {"de", "dis", "trans", "dia", "ex", "e", "mono", "uni", "bi", "di", "tri", "multi",
						 "poly", "pre", "post", "mal", "mis", "bene", "pro", "sub", "re", "inter", "intra",
						 "co", "com", "con", "col", "be", "non", "un", "in", "im", "il", "ir", "in", "im",
						 "a", "an", "anti", "contra", "counter", "en", "em"};
	
	static List<String> concatPrefix(String pre, String word) {
		List<String> res = new ArrayList<String>();
		res.add(pre + word);
		return res;
	}
	
	static List<String> concatSuffix(String word, String sfx) {
		List<String> res = new ArrayList<String>();
		int wl = word.length(), sl = sfx.length();
		res.add(word + sfx);
		
		if(!isVowel(word.charAt(wl-2)) && word.charAt(wl-1) == 'e') {
			res.add(word.substring(0, wl-1) + sfx);
		}
		
		if(!isVowel(word.charAt(wl-1)) && word.charAt(wl-1) == 'y') {
			res.add(word.substring(0, wl-1) + "i" + sfx);
		}
		
		return res;
	}
	
	public static void doProc() {
		Set<String> entrySet = new HashSet<String>();
		Set<String> allWords = new HashSet<String>();
		try {
			FileOutputStream fos = new FileOutputStream(new File("/home/canwang/downey/data/raw_db.txt"));
			IWiktionaryEdition wkt = JWKTL.openEdition(new File("/home/canwang/downey/data/output"));
			WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
			filter.setAllowedWordLanguages(Language.ENGLISH);
			int count = 0;
			for(IWiktionaryEntry ent : wkt.getAllEntries(filter)) {
				String word = ent.getWord();
				
				if(word.indexOf(' ') >= 0) continue;
				if(word.indexOf(',') >= 0) continue;
				if(word.startsWith("-")) continue;
				if(word.endsWith("-")) continue;
				List<PartOfSpeech> posList = ent.getPartsOfSpeech();
				List<IWiktionaryWordForm> wordForms = ent.getWordForms();
				//List<IWiktionaryRelation> relations = ent.getRelations(RelationType.DERIVED_TERM);
				//List<IWiktionaryRelation> relations = ent.getRelations();
				//List<IWiktionaryRelation> descRelations = ent.getRelations(RelationType.ETYMOLOGICALLY_RELATED_TERM);
				
				if(posList != null) {
					for(PartOfSpeech pos : posList) {
						if(pos == PartOfSpeech.VERB || pos == PartOfSpeech.NOUN
								|| pos == PartOfSpeech.ADJECTIVE || pos == PartOfSpeech.ADVERB
								|| pos == PartOfSpeech.CONJUNCTION || pos == PartOfSpeech.PRONOUN
								|| pos == PartOfSpeech.PREPOSITION || pos == PartOfSpeech.INTERJECTION) {
							String rootString = "[" + word + "," + word + "," + pos + "," + "ID_ROOT" + "]";
							//System.out.println(word + pos);
							if(wordForms != null) {
								insertEntry(fos, rootString, entrySet);
								allWords.add(word);
							} else if(pos == PartOfSpeech.ADJECTIVE || pos == PartOfSpeech.ADVERB
									|| pos == PartOfSpeech.CONJUNCTION || pos == PartOfSpeech.PRONOUN
									|| pos == PartOfSpeech.PREPOSITION || pos == PartOfSpeech.INTERJECTION) {
								insertEntry(fos, rootString, entrySet);
								allWords.add(word);
							}
						}
						
						if(pos == PartOfSpeech.VERB) {
							if(wordForms != null) {
								for(IWiktionaryWordForm form : wordForms) {
									String currentForm = form.getWordForm();
									GrammaticalPerson person = form.getPerson();
									GrammaticalTense tense = form.getTense();
									NonFiniteForm nff = form.getNonFiniteForm();
									if(currentForm == null) continue;
									String outString = "[" + word + "," + currentForm + "," + pos + "," + getVerbID(person, tense, nff) + "]";
									
									insertEntry(fos, outString, entrySet);
									allWords.add(currentForm);
								}
							}
						} else if(pos == PartOfSpeech.NOUN) {
							if(wordForms != null) {
								for(IWiktionaryWordForm form : wordForms) {
									String currentForm = form.getWordForm();
									if(currentForm == null || currentForm.equals("")) continue;
									String outString = "[" + word + "," + currentForm + "," + pos + "," + getNounID(form.getNumber()) + "]";
									
									insertEntry(fos, outString, entrySet);
									allWords.add(currentForm);
								}
							}
						} else if(pos == PartOfSpeech.ADJECTIVE || pos ==PartOfSpeech.ADVERB) {
							if(wordForms != null) {
								for(IWiktionaryWordForm form : wordForms) {
									String currentForm = form.getWordForm();
									if(currentForm == null || currentForm.equals("") || currentForm.indexOf(" ") >= 0) continue;
									String  idVal = getAdjOrAdvID(form.getDegree());
									if(idVal == "ERROR") continue;
									String outString = "[" + word + "," + currentForm + "," + pos + "," + idVal + "]";
									
									insertEntry(fos, outString, entrySet);
									allWords.add(currentForm);
								}
							}
						} else {
						}
						/*
						System.out.println("=>" + word + " rel " + relations);
						if(relations != null && relations.size() > 0) {
							System.out.println("=>" + word);
							for(IWiktionaryRelation rel : relations) {
								String curString = rel.getTarget();
								if(curString.indexOf(" ") >= 0) continue;
								if(curString.startsWith("-")) continue;
								if(curString.endsWith("-")) continue;
								String outString = "[" + word + "," + curString + "," + pos + "," + "ID_DERIVED" + "]";
								insertEntry(fos, outString, entrySet);
							}
						}
						
						if(descRelations != null && descRelations.size() > 0) {
							//System.out.println("=>" + word);
							for(IWiktionaryRelation rel : descRelations) {
								String curString = rel.getTarget();
								if(curString.indexOf(" ") >= 0) continue;
								if(curString.startsWith("-")) continue;
								if(curString.endsWith("-")) continue;
								String outString = "[" + word + "," + curString + "," + pos + "," + "ID_RELATED" + "]";
								insertEntry(fos, outString, entrySet);
							}
						}
						*/
						
					}
					
				}
				count++;
				if(count % 1000 == 0) {
					System.out.println("Done => " + count);
				}
				//if(count > 100) return;
			}
			System.out.println("Generate Done!!! => total " + count);
			/*
			for(String word : allWords) {
				if(word == null || word.equals("") || word.length() < 3) continue;
				
				// try prefix
				for(String pre : PREFIXS) {
					String newWord = concatPrefix(pre, word);
					if(allWords.contains(newWord)) {
						insertEntry(fos, "["+word+","+newWord+","+"DERIVED,ID_PREFIX]", entrySet);
					}
				}
				
				
				// try suffix
				for(String sfx : SUFFIXS) {
					List<String> newWords = concatSuffix(word, sfx);
					for(String newWord : newWords) {
						if(allWords.contains(newWord)) {
							insertEntry(fos, "["+word+","+newWord+","+"DERIVED,ID_SUFFIX]", entrySet);
						}
					}
				}
				count++;
				if(count % 1000 == 0) {
					System.out.println("prefix and suffix Done => " + count);
				}
			}
			*/
			wkt.close();
			System.out.println("Generate Done!!! => total " + count);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
