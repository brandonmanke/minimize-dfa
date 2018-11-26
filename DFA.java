/**
 * @author Brandon Manke
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class DFA {

  private int numberOfStates;
  private int initialState;
  private HashSet<String> sigma = new HashSet<>();
  private HashSet<Integer> finalStates = new HashSet<>();
  private HashMap<Integer, HashMap<String, Integer>> transitionMap = new HashMap<>();

  public DFA() {}

  public DFA(
    int numberOfStates, 
    int initialState, 
    HashSet<String> sigma,
    HashSet<Integer> finalStates,
    HashMap<Integer, HashMap<String, Integer>> transitionMap) {
      this.numberOfStates = numberOfStates;
      this.initialState = initialState;
      this.sigma = sigma;
      this.finalStates = finalStates;
      this.transitionMap = transitionMap;
    }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception(
        "Invalid input.\n Use: java DFA {dfaFile} {inputStringsFile}"
      );
    }
    String dfaFileName = args[0];
    DFA dfa = parseDFAFile(dfaFileName);
    //System.out.println(dfa.toString());
    DFA minimized = dfa.minimize();
    //System.out.println(minimized.toString());
  }

  public DFA minimize() {
    return new DFA();
  }

  public static DFA parseDFAFile(String fileName) {
    File file = new File(fileName);
    int nstates = 0;
    int initState = 0;
    HashSet<String> sigma = new HashSet<>();
    HashSet<Integer> accepting = new HashSet<>();
    HashMap<Integer, HashMap<String, Integer>> dfa = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String s;
      while ((s = br.readLine()) != null) {
        s = s.trim();
        if (s.isEmpty() || s.charAt(0) == '-') {
          continue;
        } else if (!s.contains(":") && s.charAt(0) != '-')  {
          nstates = Integer.parseInt(s);
        } else if (s.contains("Sigma")) {
          int index = s.indexOf(':');
          StringTokenizer st = 
            new StringTokenizer(s.substring(index + 1), " ", false);
          while (st.hasMoreTokens()) {
            sigma.add(st.nextToken());
          }
        } else if (s.contains("Initial")) {
          int index = s.indexOf(':');
          initState = Integer.parseInt(s.substring(0, index));
        } else if (s.contains("Accepting")) {
          int index = s.indexOf(':');
          String[] arr = s.substring(0, index).split(",");
          for (String str : arr) {
            accepting.add(Integer.parseInt(str));
          }
        } else {
          // transition
          int index = s.indexOf(':');
          int state = Integer.parseInt(s.substring(0, index));
          HashMap<String, Integer> transitions = new HashMap<>();
          StringTokenizer st = 
            new StringTokenizer(s.substring(index + 1), " ", false);
          Iterator<String> iter = sigma.iterator();
          while (st.hasMoreTokens() && iter.hasNext()) {
            int n = Integer.parseInt(st.nextToken());
            transitions.put(iter.next(), n);
          }
          dfa.put(state, transitions);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new DFA(nstates, initState, sigma, accepting, dfa);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Number of States: ");
    sb.append(numberOfStates);
    sb.append("\nInitial State: ");
    sb.append(initialState);
    sb.append("\nSigma: ");
    sb.append(sigma.toString());
    sb.append("\nTransitions:\n");
    sb.append(transitionMap.toString());
    sb.append("\nAccepting State(s): ");
    sb.append(finalStates.toString());
    return sb.toString();
  }
}
