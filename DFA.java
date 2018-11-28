/**
 * @author Brandon Manke
 * Minimize DFA using Hopcroft's Algorithm
 */
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class DFA {

  private int numberOfStates;
  private int initialState;
  private Set<String> sigma = new HashSet<>();
  private Set<Integer> finalStates = new HashSet<>();
  private Set<Integer> states = new HashSet<>(); // set of all states
  private Map<Integer, HashMap<String, Integer>> transitionMap = new HashMap<>();

  public DFA() {}

  public DFA(
      int numberOfStates, 
      int initialState, 
      Set<String> sigma,
      Set<Integer> finalStates,
      Set<Integer> states,
      Map<Integer, HashMap<String, Integer>> transitionMap) {
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
    String inputStringFile = args[1];
    try {
      DFA dfa = parseDFAFile(dfaFileName);
      //System.out.println(dfa.toString());
      //System.out.println();
      DFA minimized = dfa.minimize();
      System.out.println("\nMinimized DFA from " + dfaFileName + ":\n");
      System.out.println(minimized.formattedPrint());
      minimized.processInputStrings(inputStringFile);
      //System.out.println(minimized.toString());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  // Hopcroft's Algorithm
  public DFA minimize() {
    ArrayList<Set<Integer>> P = new ArrayList<>();
    P.add(finalStates);
    P.add(difference(states, finalStates));
    ArrayList<Set<Integer>> W = new ArrayList<>(); // could be stack
    W.add(finalStates);
    while (!W.isEmpty()) {
      Set<Integer> A = W.remove(0);
      //System.out.println("A: " + A.toString());
      for (String symbol : sigma) {
        // set of states for which delta(qi, symbol) = a in A, (if true, add qi to X)
        Set<Integer> X = new HashSet<>();
        for (Map.Entry<Integer, HashMap<String, Integer>> entry : transitionMap.entrySet()) {
          int qi = entry.getKey();
          HashMap<String, Integer> transition = entry.getValue();
          //System.out.println("Transition: " + transition.toString());
          int state = transition.get(symbol);
          if (A.contains(state)) {
            X.add(qi);
          }
        }
        if (X.isEmpty()) {
          continue;
        }
        //System.out.println("X: " + X.toString());
        ArrayList<Set<Integer>> newP = new ArrayList<>(P);
        for (Set<Integer> Y : P) {
          boolean removeYinP = false;
          boolean removeYinW = false;
          Set<Integer> intersectionSet = intersection(X, Y);
          Set<Integer> diffSet = difference(Y, X);
          if (!intersectionSet.isEmpty() && !diffSet.isEmpty()) {
            newP.add(intersectionSet);
            newP.add(diffSet);
            removeYinP = true;
          }

          if (W.contains(Y)) { // unsure if this works..
            W.add(intersectionSet);
            W.add(diffSet);
            removeYinW = true;
          } else {
            if (intersectionSet.size() <= diffSet.size()) {
              W.add(intersectionSet); // add X ∩ Y to W
            } else {
              W.add(diffSet); // add X \ Y to W
            }
          }

          if (removeYinP) {
            newP.remove(Y);
          }

          if (removeYinW) {
            W.remove(Y);
          }
        }
        P = newP;
      }
    }
    //System.out.println("=========== P:");
    //System.out.println("P Size: " + P.size());
    //System.out.println(P.toString());
    DFA minimizedDfa = constructMinimizedDfa(P);
    return minimizedDfa;
  }

  // Constructed minimized dfa based off of partition from Hopcroft's Algorithm
  public DFA constructMinimizedDfa(ArrayList<Set<Integer>> P) {
    int newNumOfStates = 0;
    int newInitialState = 0;
    Set<Integer> newStates = new HashSet<>();
    Set<Integer> newFinalStates = new HashSet<>();
    Map<Integer, HashMap<String, Integer>> newTransitionMap = new HashMap<>();
    for (int i = 0; i < P.size(); i++) {
      Set<Integer> newState = P.get(i);
      if (newState.isEmpty()) {
        continue;
      }
      if (newState.contains(this.initialState)) {
        newInitialState = i;
      }
      for (int state : newState) {
        if (this.finalStates.contains(state)) {
          newFinalStates.add(i);
        }
      }
      newNumOfStates++;

      // We only need one element from this set since they
      // have already been reduced at this point.
      int first = newState.iterator().next();
      HashMap<String, Integer> transitions = this.transitionMap.get(first);
      HashMap<String, Integer> newTransitions = new HashMap<>();
      for (String symbol : this.sigma) {
        int state = transitions.get(symbol); // old transition: delta(i, symbol) = state
        for (int j = 0; j < P.size(); j++) {
          Set<Integer> otherNewState = P.get(j);
          if (otherNewState.contains(state)) {
            newTransitions.put(symbol, j);
          }
        }
      }
      // add new transitions for current state to transition map
      newStates.add(i);
      newTransitionMap.put(i, newTransitions);
    }

    return new DFA(
      newNumOfStates, 
      newInitialState, 
      this.sigma, 
      newFinalStates,
      newStates,
      newTransitionMap
    );
  }

  public void processInputStrings(String fileName) throws Exception {
    File file = new File(fileName);
    System.out.println("The following strings are accepted:");
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String s;
      while ((s = br.readLine()) != null) {
        s = s.trim();
        if (s.isEmpty()) {
          continue;
        }
        int currentState = initialState;
        boolean valid = true;
        for (Character c : s.toCharArray()) {
          HashMap<String, Integer> transitions = this.transitionMap.get(currentState);
          String symbol = Character.toString(c);
          if (!transitions.containsKey(symbol)) {
            valid = false;
            break;
          }
          currentState = transitions.get(symbol);
        }
        if (this.finalStates.contains(currentState) && valid) {
          System.out.println(s);
        }
      }
    }
  }

  public static DFA parseDFAFile(String fileName) throws Exception {
    File file = new File(fileName);
    int nstates = 0;
    int initState = 0;
    Set<String> sigma = new HashSet<>();
    Set<Integer> accepting = new HashSet<>();
    Set<Integer> states = new HashSet<>();
    Map<Integer, HashMap<String, Integer>> dfa = new HashMap<>();
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
      //e.printStackTrace();
      throw e;
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

  public String formattedPrint() {
    final String space = "    ";
    StringBuffer sb = new StringBuffer();
    sb.append("Sigma:");
    this.sigma.forEach(symbol -> {
      sb.append(space);
      sb.append(symbol);
    });
    int lineLength = sb.toString().length();
    printLine(sb, lineLength);
    for (int state : this.states) {
      sb.append(space);
      sb.append(state);
      sb.append(":");
      HashMap<String, Integer> transitions = this.transitionMap.get(state);
      this.sigma.forEach(symbol -> {
        sb.append(space);
        sb.append(transitions.get(symbol));
      });
      sb.append("\n");
    }
    printLine(sb, lineLength);
    sb.append(this.initialState);
    sb.append(":");
    sb.append(" Initial State\n");
    this.finalStates.forEach(state -> {
      sb.append(state);
      sb.append(",");
    });
    sb.append(": Accepting State(s)\n");
    return sb.toString();
  }

  private static void printLine(StringBuffer sb, int len) {
    sb.append("\n");
    for (int i = 0; i < len; i++) {
      sb.append("-");
    }
    sb.append("\n");
  }

  // Set related operations
        
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
