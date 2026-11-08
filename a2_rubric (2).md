# A2 Rubric Explanation

This document is part of your second submission.  Complete it and include it in your submission zip, alongside your interpreter and example programs.

## How it works

The rubric explanation is the set of questions below.  There is one section for each rubric item of Submission Two.  These are not graded directly, but your answers help your marker award you the marks for each rubric item, so write your answers below each question text in markdown format and point your marker to where the evidence lives in your submission.

Give your marker everything they need to compile, run, and understand your language.  For each rubric item, we recommend you point your marker to the example program you submitted which demonstrates that item as the simplest way to show you have achieved it.

## How can your marker compile and run your interpreter?

Give the working folder and the exact command(s) your marker needs to compile and run your interpreter, and where in the zip it lives.

### Your answer

All source files are flat in the root of the zip (no packages, no subfolders). From that folder:

```
javac *.java
java lox <program file> [rainfall_mm]
```

Examples:

```
java lox Program2.txt        (rainfall defaults to 1 mm)
java lox Program5.txt 25     (25 mm of rainfall)
```

Running `java lox` with no arguments starts an interactive prompt (each line is run on its own).

The entry point is `lox.java`. The interpreter is `Interpreter.java`, supported by `Environment.java`, `RuntimeError.java` and `Release.java`. The scanner and parser (`Scanner.java`, `Parser.java`, `Expr.java`, `Stmt.java`) are from Submission One with small extensions. Tested with JDK 21; the interpreter uses no syntax newer than JDK 8.

- NOTE: `[rainfall_mm]` is optional (default 1) and may be a whole number or decimal, e.g. 12 or 12.5.

## Give details of each of the example programs you expect your marker to use

For each example program, give its file name, what it demonstrates, and how to run it, so your marker can reproduce your results.

### Your answer

- **Program1.txt**: one root river from a `rain(...)` literal, marked `output`. Shows the simplest flow. Run: `java lox Program1.txt`
- **Program2.txt**: three root rivers combined with `+` into an output river. Shows day-by-day addition of flows. Run: `java lox Program2.txt` (or `java lox Program2.txt 12` for 12 mm).
- **Program3.txt**: the Canberra system from the brief, with three dams connected using `dam1(100, 50) from googong` syntax. With these settings the dams pass water through, showing that dam syntax and arguments parse and run end to end. Run: `java lox Program3.txt`
- **Program4.txt**: one creek fed into several dams: `closed` (no-flow), `half` (half-flow), `open` (full-flow), `gate` (release set as a percentage, clamped at 100%) and `greedy` (asks for 3x the inflow and is capped by the water available). Run: `java lox Program4.txt`
- **Program5.txt**: a "smart" dam whose release depends on inflow, its level, its capacity and the rainfall. Run `java lox Program5.txt` and `java lox Program5.txt 25` to compare.
- **Program6.txt**: the three Googong dam versions from the week 11 application exercise — blocks half; blocks half unless rain > 10 mm; blocks half unless inflow > 10, then blocks 75%. Run `java lox Program6.txt` and `java lox Program6.txt 12` to compare.

- NOTE: Programs 1–3 are carried over from Submission One (Program3 updated to give each dam its arguments). **Program4.txt, Program5.txt and Program6.txt were written specifically for Submission Two** to demonstrate dam evaluation: no-flow, half-flow and full-flow dams, the release cap, and dam output depending on multiple factors.

## Interpreting simple river flows (2)

What does your interpreter output when it finishes evaluating a river system, indicating at least one of the flows in the system?  How are rainfall and river catchment size made user-definable in your language?  Point your marker to the example program and the interpreter files that demonstrate this.

### Your answer

When the program finishes, the interpreter prints the rainfall used and the 10-day flow of every declared river, in declaration order, and marks the river declared with `output` as `<-- OUTPUT`. For example, `java lox Program2.txt` prints:

```
Rainfall: 1.0 mm
googong: [5.00, 2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01]
jerrabombarra: [1.67, 1.11, 0.74, 0.49, 0.33, 0.22, 0.15, 0.10, 0.07, 0.04]
upper_molongolo: [4.00, 2.00, 1.00, 0.50, 0.25, 0.13, 0.06, 0.03, 0.02, 0.01]
central_molongolo: [10.67, 5.61, 2.99, 1.62, 0.89, 0.50, 0.29, 0.17, 0.10, 0.06]   <-- OUTPUT
```

**Rainfall** is user-definable as the optional second command-line argument (`java lox Program2.txt 12`); every flow scales with it.

**Catchment size** is user-definable as the third argument of the flow literal `rain(start, spread, magnitude)`: the total water a catchment produces is `rainfall x magnitude`.

Evidence: `Program1.txt`, `Program2.txt`; `Interpreter.java` (`visitflowLiteralExpr`, `visitRiverStmt`, `report`), `lox.java` (`main` reads the rainfall argument).

## Multi-day flow modelling (2)

How does your language model the flow over multiple days, and how do the flows carry over between days?  Point your marker to the output of your example program showing the predicted flows on all those days.

### Your answer

Every flow is a 10-day array, one value per day.

`rain(start, spread, magnitude)` models water reaching the river from the catchment over time. No water arrives before day `start`. From then on, each day a fraction `1 / (spread + 1)` of the water still on the catchment flows into the river, and **the rest carries over to the next day**. `spread 0` sends all the water on day `start`; a larger spread gives a longer, slower tail.

Flows combine with `+`, `-`, `*` and `/` day by day (element-wise), so the flow into a river is the sum of its feeding rivers on each day.

Dams also carry state between days: each dam's stored level is updated every day (`level = level + inflow - release`), and that level is used the next day.

Evidence: the output of `java lox Program2.txt` above (all 10 days for each river), and `java lox Program5.txt`, where the dam stores water on day 1, fills on day 2 and spills from day 3:

```
catchment: [25.00, 12.50, 6.25, 3.13, 1.56, 0.78, 0.39, 0.20, 0.10, 0.05]
downstream: [5.00, 2.50, 6.25, 3.13, 1.56, 0.78, 0.39, 0.20, 0.10, 0.05]   <-- OUTPUT
```

## Dam syntax and grammar (2)

Provide the full grammar for your language, including your dam syntax.  It must be complete and match your example programs so the marker can write their own programs.  Which of your example programs demonstrate your dam syntax, and can your parser parse them?

### Your answer

```
program     → declaration* EOF ;
declaration → damDecl | varDecl | riverDecl | statement ;

damDecl     → "dam" IDENTIFIER "(" parameters? ")" block ;
parameters  → IDENTIFIER ( "," IDENTIFIER )* ;
varDecl     → "var" IDENTIFIER ( "=" expression )? ";" ;
riverDecl   → "output"? "river" IDENTIFIER "=" ( connection | expression ) ";" ;
connection  → IDENTIFIER ( "(" arguments? ")" )? "from" expression ;
arguments   → expression ( "," expression )* ;

statement   → exprStmt | block | ifStmt | releaseStmt ;
block       → "{" declaration* "}" ;
ifStmt      → "if" "(" expression ")" statement ( "else" statement )? ;
releaseStmt → "release" expression ";" ;
exprStmt    → expression ";" ;

expression  → equality ;
equality    → comparison ( ( "!=" | "==" ) comparison )* ;
comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → ( "!" | "-" ) unary | primary ;
primary     → NUMBER | STRING | "true" | "false" | "nil"
            | "(" expression ")" | IDENTIFIER | flowLiteral ;
flowLiteral → "rain" "(" expression "," expression "," expression ")" ;
```

`NUMBER` accepts integers and decimals (e.g. `10`, `0.5`). `//` starts a comment to the end of the line.

A dam is declared once with named parameters:

```
dam smart(capacity, startingLevel) { ... }
```

and used on the right of a river declaration, with one argument per parameter:

```
river downstream = smart(30, 0) from catchment;
```

Inside a dam body, the names `inflow`, `level`, `rainfall` and `day` are available automatically, and `release <expression>;` sets the dam's output for the day. The only grammar change since Submission One is that `connection` now accepts an argument list.

`Program3.txt`, `Program4.txt` and `Program5.txt` all use dam syntax, and all parse and run without errors.

## Dam evaluation (2)

How does your interpreter run the dam's algorithm, and how do downstream river flows reflect its behaviour?  Show that you have sensible behaviour for no-flow, half-flow, and full-flow dams, that you check the release fraction doesn't exceed 1, and how dam parameters are set by the user.

### Your answer

A dam declaration is stored in the environment. When a river uses `dam(args) from source`, the interpreter (`visitconnectionExpr` in `Interpreter.java`):

1. Checks the name is a dam and that the number of arguments matches the number of parameters (otherwise e.g. `Dam 'dam1' expects 2 arguments but got 0.` with the line number).
2. Evaluates the source to a 10-day inflow.
3. Sets the starting level from the parameter named `startingLevel` (0 if the dam has none).
4. For each of the 10 days, creates a fresh environment (enclosing the global one) containing the parameters, `inflow`, `level`, `rainfall` and `day`, and runs the dam body. A `release` statement stops the body and returns the amount (implemented like the book's `Return`, Chapter 10). If no `release` is reached, the dam releases 0.
5. Caps the release between 0 and `level + inflow`, so a dam can never release more water than it holds.
6. Updates `level = level + inflow - release` for the next day.

The 10 released amounts become the downstream river, so every river below the dam reflects its behaviour.

`java lox Program4.txt` shows:

```
creek: [5.00, 2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01]
no_flow: [0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00]
half_flow: [2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01, 0.00]
full_flow: [5.00, 2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01]
gate_half: [2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01, 0.00]
gate_over: [5.00, 2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01]
capped: [9.00, 2.50, 1.25, 0.63, 0.31, 0.16, 0.08, 0.04, 0.02, 0.01]   <-- OUTPUT
```

- **No-flow** (`closed`, `release 0`): downstream is all zero; the water accumulates in the dam.
- **Half-flow** (`half`, `release inflow / 2`): exactly half passes.
- **Full-flow** (`open`, `release inflow`): downstream equals the creek.
- **Release fraction ≤ 1**: the `gate` dam takes its release fraction as a percentage. `gate(50)` releases half; `gate(150)` asks for 150% and is clamped to 100%, so the fraction can never exceed 1. Independently, the interpreter caps every release at `level + inflow`: `greedy(4)` asks for 3x the inflow, but on day 1 it only has 4 stored + 5 arriving, so it releases 9 and then can only pass on what arrives.
- **Parameters** are set by the user as arguments at the point of use, e.g. `gate(50)`, `greedy(4)`, `smart(30, 0)`, so one dam behaviour can be reused with different settings.

## Dam output depends on multiple factors (2)

What does the output from your dam depend on?  It must depend on at least two of the following: the flow into the dam, today's rain, the dam level.  Explain your design decisions and implementation steps --- this explanation is worth half of this item's marks.

### Your answer

A dam's release can depend on the **inflow** (today's incoming water), its **level** (stored water), today's **rainfall**, the **day** number, and any user parameters such as **capacity**. `Program5.txt`'s smart dam uses inflow, level, rainfall and capacity:

```
if (level + inflow > capacity) {
    release level + inflow - capacity;   // spill only the excess
} else if (rainfall > 20) {
    release inflow;                      // heavy rain: pass through
} else {
    release inflow / 5;                  // store most of it
}
```

At 1 mm (`java lox Program5.txt`) the dam stores most of day 1 (releases 5.00 of 25.00), fills on day 2 and spills only the excess (2.50), then passes everything through once full. At 25 mm (`java lox Program5.txt 25`) it overflows immediately:

```
catchment: [625.00, 312.50, 156.25, 78.13, 39.06, 19.53, 9.77, 4.88, 2.44, 1.22]
downstream: [595.00, 312.50, 156.25, 78.13, 39.06, 19.53, 9.77, 4.88, 2.44, 1.22]   <-- OUTPUT
```

Same program, different behaviour, driven by inflow, level and rainfall.

**Design decisions**

- Dams are reusable behaviours with parameters, like functions, rather than one-off blocks, so capacity and starting level are set at each use. The exemplar lists this only as a possible extension.
- `startingLevel` lets a dam begin partly full, since a real dam is rarely empty when a storm arrives (the exemplar always starts at 0).
- Rules are written with `if`/`else` and an explicit `release` statement rather than `when`/`default`, so a dam reads as an ordinary decision procedure.
- `inflow`, `level`, `rainfall` and `day` are provided automatically each day rather than passed in, because they are things the dam operator observes, not settings.
- The cap is enforced by the interpreter, not by the program, so no dam can create water, whatever its rules say.

**Implementation steps**

1. Extended the `connection` grammar with an argument list (`dam1(100, 50) from googong`).
2. Added a parent (`enclosing`) environment to `Environment` so dam bodies can see outer names (Chapter 8).
3. Stored dam declarations in the environment (`visitDamStmt`).
4. Implemented `if`/`else` (Chapter 9).
5. Added a `Release` exception modelled on the book's `Return` (Chapter 10).
6. Wrote `visitconnectionExpr` to check arguments, bind parameters, run the body once per day, cap the release and carry the level forward.


## Log-book submissions (2)

Which file in the zip are your log-book entries and when did you make them?  Your teacher needs to have seen them during the semester.

### Your answer

My log-book is `Logbook_ASS2.md` in the root of the zip (Entries for weeks 8-13).


## Expressing the underlying concepts (2)

How well does your language express the underlying concepts?  Explain the keywords and syntax structures you chose and what makes it an interesting language overall.

### Your answer

The keywords map directly onto the domain:

- `river` declares a river, and `output` marks the system's final river.
- `dam` declares a dam's operating rule, and `release` is how much water it lets out.
- `from` says where a river's water comes from.
- `rain(start, spread, magnitude)` describes how a catchment responds to rain.

A program reads close to the brief's own description: `river queanbeyan = dam1(100, 50) from googong;` is "dam 1 drains to queanbeyan".

`+` is reused for confluences because the domain rule is that flows add. Flows are first-class 10-day values, so the same arithmetic works on rivers and numbers (`googong + jerrabombarra`, `2 * googong`).

Dam rules can use exactly the quantities a dam operator looks at (`inflow`, `level`, `rainfall`, `day`), and the interpreter enforces the physical limit that a dam cannot release more than it holds. This makes it an interesting language: small and domain-specific, but dams are genuinely programmable, reusable behaviours rather than fixed settings.

## Uniqueness and Creativity (2)

What did you do beyond the in-class work?  Point your marker to where it lives in your submission.  You can present your work in the week 13 class to guarantee full marks for this rubric item.

### Your answer

Beyond the in-class work and the exemplar:

- Keyword-based syntax throughout (`river`, `dam`, `release`, `from`, `output`, `rain`) instead of symbols like `<-` and `[~]@`.
- Dams are reusable, parameterised behaviours (`dam smart(capacity, startingLevel)`), instantiated with arguments at each use; the exemplar lists this only as a possible extension.
- `startingLevel` lets a dam start partly full, fixing the exemplar's assumption that every dam starts empty.
- Dam rules can use `rainfall` and `day` as well as `inflow` and `level`.
- The interpreter caps every release to the water available, so dam rules can't create water.
- Rainfall is supplied on the command line, so one program can be run under different storms (`Program5.txt` at 1 mm and at 25 mm).
- `AstPrinter` was extended in Submission One to print statements as well as expressions.

Evidence: `Interpreter.java` (`visitconnectionExpr`, `visitflowLiteralExpr`, `arth`), `Parser.java` (`riverdeclaration`), `Program4.txt` and `Program5.txt`.
