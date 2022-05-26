Dojo Toolkit Graphing Calculator Project Readme
Author: Jason Hays
Trac ID: jason_hays22

Contents:
	Expressions
	Variables, Functions, and uninitializing variables
	toFrac in GraphPro
	Numbers and bases
	Graphing equations
	Substitutes for hard to type characters
	Making Functions
	Decimal points, commas, and semicolons in different languages
	Important mathematical functions



---------------Expressions----------------
The calculator has the ability to simplify a valid expression.
With Augmented Mathematical Syntax, users are allowed to use nonstandard operators in their expressions.  Those operators include ^, !, and radical.

^ is used for exponentiation.
	It is a binary operator, which means it needs a number on both the left and right side (like multiplication and division)
	2^5 is an example of valid use of ^, and represents two to the power of five.

! is used for factorial.
	It supports numbers that are not whole numbers through the use of the gamma function.  It uses the number on its left side.  Both 2! and 2.6! are examples of valid input in America. (2,6! is valid in some nations)

radicals can be used for either square root or various other roots.
	to use it as a square root sign, there should only be a number on its right.  If you put a number on the left as well, then it will use that number as the root.

To evaluate an expression, type in a valid expression, such as 2*(10+5), into the input box.  If you are using GraphPro, then it is the smaller text box.
	After you have chosen an expression, press Enter on either your keyboard or on the lower right of the calculator.
		If it did not evaluate, make sure you correctly closed your parentheses.
	In the Standard calculator, the answer will appear in the input box, in GraphPro, the answer will appear in the larger text box above.
	On the keyboard, you can navigate through your previous inputs with the up and down arrow keys.

If you enter an operator when the textbox is empty or highlighted (like *) then Ans* should appear.  That means the answer you got before will be multiplied by whatever you input next.
So try Ans*3.  Whenever you start the calculator, Ans is set to zero.


--------------Variables, Functions, and uninitializing variables----------------
A variable is basically something that stores a value.  If you saw Ans in the previous example, you've also seen a variable.
If you want to store your own number somewhere, you'll need to use the = operator.

	Valid variable and function names include cannot start with numbers, do not include spaces, but can start with the alphabet (a-z or A-Z) and can have numbers within the names "var1" is a valid name

	Input "myVar = 2" into the textbox and press "Enter."  You've just saved a variable.  Now if you ever type myVar into an expression, 2 will appear (unless you change it to something else).
	Variables are best used to store Ans.  Ans is overridden whenever you evaluate an expression, so it is good to store the value of Ans somewhere else before it is overridden.

	If you want a variable (like myVar) to become empty, or undefined, you just need to set it equal to undefined.
	Now try "myVar = undefined"  Now myVar is no longer defined.

	Functions are very useful for finding answers and gathering data.
		You can use functions by inputting their name and their arguments.
		For this example, I'll be using the functions named "sqrt" and "pow"

		sqrt is a function with one argument.  That argument has a name too, its name is 'x.' x is a very common name amongst built in functions
			So, let's run a function.  Input "sqrt" then input a left parenthesis (all arguments of a function go within parentheses).  Now type a value for x, like 2.
			Now close the parentheses with the right parenthesis.  If you used 2, you should have "sqrt(2)" in the text box.  If you press enter, you should get the square root of 2 back from the calculator.

		Now for "pow" it has two arguments 'x' and 'y'
			Type in "pow(" and pick a value for 'x' (I am picking 2 again)
			but now, you need to separate the value you gave x with a list separator.  Depending on your location, it is either a comma or a semicolon.  I'm in America, and I use commas.
			by this point, I have "pow(2,"
			Now we need a value for 'y' (I'm using 3).  Put a ')' and now I have 'pow(2,3)'
			Press Enter, and, following my example, you should get 8

		In this calculator, there are several ways to input arguments.
			You've already seen the first way, just input numbers in a specific order based on the names.

			The second way is with an arbitrary order, and storage.
			With 'pow' I can input "pow(y=3, x=2)" and get the exact answer as before.  x and y will retain their assigned values, so you will need to set them to undefined it you want to try the next way.

			The third way is to let the calculator ask you for the values.  Input "pow()"  If the values have been assigned globally, then it will use those values, but otherwise, it will ask for values of x and y.  They will not be stored globally this way.

	I'll go ahead and mention that because of the way the calculator parses, underscores should not be used to name a variable like _#_ (where # is an integer of any length) 

---------------toFrac()----------------
toFrac is a function that takes one parameter, x, and converts it to a fraction for you.  It is only in GraphPro, not the Standard mode.
	It will try to simplify pi, square roots, and rational numbers where the denominator is less than a set bound (100 right now).
	Immediately after the calculator starts, toFrac may seen slow, but it just needs to finish loading when the calculator starts.  After that, it will respond without delay.
	For an example, input "toFrac(.5)" or (,5 for some).  It will return "1/2"
	For a more complicated example, input "toFrac(atan(1))" to get back "pi/4"  (atan is also known as "arc tangent" or "inverse tangent")

--------------Numbers and bases----------------
This calculator supports multiple bases, and not only that, but non integer versions of multiple bases.
	What is a base?  Well, the numbers you know and love are base 10.  That means that you count to all of the numbers up until 10 before you move on to add to the tenths place.
		So, what about base 2?  All of the numbers up until 2 are 0 and 1.  If you want to type a base 2 number into the calculator, simply input "0b" (meaning base 2) followed by some number of 1's and 0's. 0b101 is 5 in base 10

		Hexadecimal is 0x, and octal is 0o, but i won't go into too much detail on those here.
		If you want an arbitrary integer base, type the number in the correct base, insert '#' and put the radix on the end.  ".1#3" is the same as 1/3 in base 10
			Because there is not yet cause for it, you cannot have a base that is not a whole number.

--------------Graphing Equations----------------
First thing is first, in GraphPro only, the "Graph" button in the top left corner opens the Graph Window
	So, now you should see a single text box adjacent to "y="
	Type the right side of the equation using 'x' as the independent variable.
	"sin(x)" for example.  To Graph it, make sure the checkbox to the left of the equation is selected, and press the Draw Selected button.
	You can change the color in the color tab.  By default, it is black.  Under window options, you can change the window size and x/y boundaries

	Let's add a second function.  Go to the Add Function button, select the mode you want, and press Create.  Another input box will appear.
	If you selected x= as the Mode, then y is the independent variable for the line (an example is "x=sin(y)").
		If you want to erase, check the checkboxes you want to erase, and press "Erase Selected"
		And similarly, Delete Selected will delete the chosen functions

	"Close" will terminate the Graph Window completely

---------------Substitutes for hard to type characters---------------
Some characters are not simple to add in for keyboard users, so there are substitutes that are much easier to add into the text box.

	pi or PI can be used in place of the special character for it.
	For epsilon, eps or E can be used.
	radical has replacement functions.  sqrt(x) or pow(x,y) can be used instead.

--------------Making Functions-----------------
My favorite part.  Before we start, I'll mention that Augmented Mathematical Syntax is allowed in the Function Generator (yay).
	Ok, now the bad news: to prevent some security issues, keywords new and delete are forbidden.
	Sorry, it is a math calculator, not a game container; not that that would be so bad, but it is to keep it from being used for some evil purposes.

	Ok, onto function making.  Most JavaScript arithmetic is supported here, but, some syntax was overlapping mathematical syntax, so ++Variable no longer increases the contents of Variable because of ++1, but Variable++ does increase it (same deal with --)
		Strings have incredibly limited support. Objects have near zero support
	So, let's make a Function:
		Press the "Func" button.  A Function Window should pop up.
		Enter a name into the "functionName" box.  (it must follow the name guidelines in the variables section)  I'm putting myFunc
		Enter the variables you want into the arguments box (I'll put "x,y" so I have two arguments x and y)
		Now enter the giant text box.
		Type "return " and then the expression you want to give to the calculator.  I'm putting "return x*2 + y/2"
		Then press Save.  Now your function should appear in the functionName list and you can call it in the Calculator.

		If you want to Delete a function you made, select it in the function Name list, and then press Delete.
		If you altered a previously saved function (and haven't saved over the old one) you can reset the text back to its original state with the Reset button.
		Clear will empty out all of the text boxes in the Function Window
		Close terminates the Function Window

---------------Decimal points, commas, and semicolons in different languages----------------
In America, 3.5 is three and one half. Comma is used to separate function parameters and list members.

In some nations, 3,5 is three and one half.  In lists, ;'s are used to separate its members.
So, when you evaluate expressions, 3,5 will be valid, but in the function generator, some ambiguous texts prevent me from allowing the conversion of that format to JavaScript.  So I cannot parse it in the Function Generator.
And here is my example:
var	i = 3,5;
	b = 2;
I cannot discern whether the semicolon between i and b are list separators or the JavaScript character for end the line.  b could be intended as a global variable, and i is a local variable, but I don't know that.  So language conversion isn't supported in Function Making.

--------------Important mathematical Functions---------------
Here is a list of functions you may find useful and their variable arguments:

sqrt(x)	returns the square root of x
x is in radians for all trig functions
sin(x)	returns the sine of x
asin(x)	returns the arc sine of x
cos(x)	returns the cosine of x
acos(x)	returns the arc cosine of x
tan(x)	returns the tangent of x
atan(x)	returns the arc tangent of x

atan2(y, x)	returns the arc tangent of y and x
Round(x)	returns the rounded integer form of x
Int(x)		Cuts off the decimal digits of x
Ceil(x)		If x has decimal digits, get the next highest integer

ln(x)	return the natural log of x
log(x)	return log base 10 of x
pow(x, y) return x to the power of y

permutations(n, r)	get the permutations for n choose r
P(n, r) see permutations
combinations(n, r)	get the combinations for n choose r
C(n, r) see combinations

toRadix(number, baseOut)	convert a number to a different base (baseOut)
toBin(number)			convert number to a binary number
toOct(number)			convert number to an octal number
toHex(number)			convert number to a hexadecimal number
