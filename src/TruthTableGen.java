import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.JOptionPane;

public class TruthTableGen {
	private static String input;
	private static StringBuffer postFixed = new StringBuffer();
	private static boolean[][] truthTable;
	private static int operandCount;
	private static List<Character> operands;


	public static void main(String[] args){		
		displayInstructions();
		takeInput();
		convertToPostFix();	
		generateTruthTable();		
	}

	

	private static void displayInstructions() {
		System.out.println("* is xor, "
				+ "^ is and, "
				+ "| is or, "
				+ "> is if-then, "
				+ "- is negation, "
				+ "= is Iff, ");
	}

	private static void displayTruthTable() {
		int columnWidth = input.length() + 2;
		for(Character c : operands){
			System.out.printf("%3c",  c);
		}
		System.out.printf("%"+columnWidth+"s\n",  input);
		for(boolean[] i : truthTable){
			for(boolean j: i){
				char value = j? 'T':'F';
				System.out.printf("%3c", value);
			}
			System.out.println();
		}
	}

	private static void generateTruthTable() {
		int row = (int) Math.pow(2, operandCount);
		int column = operandCount + 1;		
		rowCounter=0;		
		truthTable = new boolean[row][column];
		setFirstRowTrue();
		traverse(operandCount);
		evaluateExpression(row);
		displayTruthTable();
	}
	private static void evaluateExpression(int row) {
		int expLength = postFixed.length();
		Stack<Boolean> boolStack = new Stack<Boolean>();
		for(int i=0 ; i<row ; i++){
			for( int j=0 ; j<expLength; j++ ){
				Character operator = postFixed.charAt(j);
				if(isOperand(operator)){
					boolean current = truthTable[i][operands.indexOf(operator)];
							boolStack.push(current);
				}else{
					operate(operator, boolStack);
				}
			}
			truthTable[i][operandCount] = boolStack.pop();
			if(!boolStack.isEmpty()) invalidInput();
		}
	}

	private static void operate(Character operator, Stack<Boolean> boolStack) {
		boolean stackHead;
		boolean stackNeck;
		boolean processed;
		
		switch(operator){
		case '-' :
			stackHead = !boolStack.pop();
			boolStack.push(stackHead);
			break;
		case '*' :
			stackHead = boolStack.pop();
			stackNeck = boolStack.pop();
			processed = stackHead!=stackNeck;
			boolStack.push(processed);
			break;
		case '|' :
			stackHead = boolStack.pop();
			stackNeck = boolStack.pop();
			processed = stackHead || stackNeck;
			boolStack.push(processed);
			break;
		case '^' :
			stackHead = boolStack.pop();
			stackNeck = boolStack.pop();
			processed = stackHead && stackNeck;
			boolStack.push(processed);
			break;
		case '=' :
			stackHead = boolStack.pop();
			stackNeck = boolStack.pop();
			processed = stackHead == stackNeck;
			boolStack.push(processed);
			break;
		case '>' :
			stackHead = boolStack.pop();
			stackNeck = boolStack.pop();
			processed = !stackNeck || stackHead;
			boolStack.push(processed);
			break;
		}
	}

	private static void setFirstRowTrue() {
		Arrays.fill(truthTable[0], true);
	}

	private static int rowCounter;
	private static void traverse(int n) {
		if(n==1){
			rowCounter++;
			switchAllColumns(n);
			return;
		}
		traverse(n-1);
		rowCounter++;
		switchAllColumns(n);
		traverse(n-1);
	}



	private static void switchAllColumns(int n) {
		int stop = operandCount-n;
		copyPreviousRowToCurrent();
		for(int i=operandCount-1 ; i >= stop ; i--){
			truthTable[rowCounter][i] = !truthTable[rowCounter-1][i];
		}
	}

	private static void copyPreviousRowToCurrent() {
		for(int i=0 ; i<operandCount ; i++){
			truthTable[rowCounter][i] = truthTable[rowCounter-1][i];
		}
	}

	private static void convertToPostFix() {
		operandCount = 0;
		operands = new ArrayList<Character>();
		Stack<Character> operatorStack = new Stack<Character>();
		HashMap<Character, Integer> precedenceMap = new HashMap<Character, Integer>();
		buildPrecedenceMap(precedenceMap);
		for(int i=0 ; i<input.length(); i++){
			Character c = input.charAt(i);
			
			//skip spaces
			if(c==' ') continue; 
			else if(isOperand(c)){
				boolean isConsecutiveOperand = i>0 && isOperand(input.charAt(i-1));
				if(isConsecutiveOperand) invalidInput();
				postFixed.append(c);
				if(!operands.contains(c)){
					operandCount++;
					operands.add(c);
				}
			}
			else if(c=='(') operatorStack.push('(');
			else if(c==')') popAllTheWay( operatorStack );			
			else if(isOperator(c, precedenceMap)){
				boolean isConsecutiveOperator = i>0 && isOperator(input.charAt(i-1), precedenceMap);
				if(isConsecutiveOperator) invalidInput();
				processOperator(c, operatorStack, precedenceMap);
			}
			else invalidInput();
		}
		popAllTheWay(operatorStack);

	}

	private static void invalidInput() {
		System.out.println("invalid input!");
		System.exit(0);
	}

	private static void buildPrecedenceMap(HashMap<Character, Integer> precedenceMap) {
		Integer veryhigh = 0;
		Integer high = 1;
		Integer medium = 2;
		Integer low = 3;
		precedenceMap.put('-', veryhigh);
		precedenceMap.put('^', high);
		precedenceMap.put('|', medium);
		precedenceMap.put('*', medium);
		precedenceMap.put('>', low);
		precedenceMap.put('=', low);
	}

	private static boolean isOperator(Character c, HashMap<Character, Integer> precedenceMap) {
		return precedenceMap.containsKey(c);
	}

	private static void processOperator(Character operator, Stack<Character> operatorStack, 
			HashMap<Character, Integer> precedenceMap) {

		while( !charIsGreaterThanHead(operator, operatorStack, precedenceMap) &&
				operatorStack.peek() != '(' &&
				!operatorStack.isEmpty()){
			postFixed.append(operatorStack.pop());
		}			
		operatorStack.push(operator);
	}



	private static boolean charIsGreaterThanHead(Character c, Stack<Character> operatorStack,
			HashMap<Character, Integer> precedenceMap) {
		if(operatorStack.isEmpty() || operatorStack.peek()=='(') return true;

		int a = precedenceMap.get(c);
		int b = precedenceMap.get(operatorStack.peek());
		return a < b;
	}

	private static void popAllTheWay(Stack<Character> operatorStack) {
		while( !operatorStack.isEmpty()){
			if(operatorStack.peek()=='('){
				operatorStack.pop();
				return;
			}
			postFixed.append(operatorStack.pop());
		}
	}

	private static boolean isOperand(Character c) {
		return Character.isLetter(c);
	}

	private static void takeInput() {
		try(Scanner scan = new Scanner(System.in)){
			input = scan.nextLine();
			checkParentheses();
			scan.close();
		}
	}
	private static void checkParentheses() {
		int count = 0;
		for(int i=0 ; i<input.length(); i++){
			if(input.charAt(i)=='(' ){
				count++;
			}else if(input.charAt(i)==')'){
				count--;
			}			
		}
		if (count!=0) invalidInput();
	}
}
