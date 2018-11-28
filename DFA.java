/**
 * @author Brandon Manke
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class DFA {

  private int numberOfStates;
  private int initialState;
  private Set<String> sigma = new HashSet<>();
  private Set<Integer> finalStates = new HashSet<>();
  private Set<Integer> states = new HashSet<>(); // set of all states
  private HashMap<Integer, HashMap<String, Integer>> transitionMap = new HashMap<>();

  public DFA() {}

  public DFA(
      int numberOfStates, 
      int initialState, 
      Set<String> sigma,
      Set<Integer> finalStates,
      Set<Integer> states,
      HashMap<Integer, HashMap<String, Integer>> transitionMap) {
    this.numberOfStates = numberOfStates;
    this.initialState = initialState;
    this.sigma = sigma;
    this.finalStates = finalStates;
    this.states = states;
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
    System.out.println(dfa.toString());
    System.out.println();
    DFA minimized = dfa.minimize();
    //System.out.println(minimized.toString());
  }

  public DFA minimize() {
    ArrayList<Set<Integer>> P = new ArrayList<>();
    P.add(finalStates);
    P.add(difference(states, finalStates));
    ArrayList<Set<Integer>> W = new ArrayList<>(); // could be stack
    W.add(finalStates);
    while (!W.isEmpty()) {
      Set<Integer> A = W.remove(0);
      for (String symbol : sigma) {

      }
    }
    return new DFA();
  }

  public static DFA parseDFAFile(String fileName) {
    File file = new File(fileName);
    int nstates = 0;
    int initState = 0;
    Set<String> sigma = new HashSet<>();
    Set<Integer> accepting = new HashSet<>();
    Set<Integer> states = new HashSet<>();
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
          states.add(state);
          dfa.put(state, transitions);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new DFA(nstates, initState, sigma, accepting, states, dfa);
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

    //sb.append("\nAll states: (Q):\n");
    //sb.append(states.toString());
    return sb.toString();
  }

  public static Set<Integer> union(Set<Integer> s1, Set<Integer> s2) {
    Set<Integer> union = new HashSet<Integer>(s1);
    union.addAll(s2);
    return union;
  }

  public static Set<Integer> intersection(Set<Integer> s1, Set<Integer> s2) {
    Set<Integer> intersection = new HashSet<Integer>(s1);
    intersection.retainAll(s2);
    return intersection;
  }

  public static Set<Integer> difference(Set<Integer> s1, Set<Integer> s2) {
    Set<Integer> difference = new HashSet<Integer>(s1);
    difference.removeAll(s2);
    return difference;
  }
}
