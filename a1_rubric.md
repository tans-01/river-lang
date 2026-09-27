# A1 Rubric Explanation

This document is part of your first submission.  Complete it and include it in your submission zip, alongside your parser, example programs.

## How it works

The rubric explanation is the set of questions below.  The first set, the basic questions, is graded directly and is worth 10\% of your marks for this submission.  Answer them accurately to earn those marks.

The remaining sections ask one question for each of the other rubric items.  These are not graded directly, but your answers help your marker award you the marks for each rubric item, so write your answers below each question text in markdown format and point your marker to where the evidence lives in your submission.

## Basic questions (10)

1. Which chapter of the book did you use as the starting point for your solution?

### Your answer

Chapter 4 (Scanning) and Chapter 5 (Representing Code) for the scanner and AST class generation, and Chapter 6 (Parsing Expressions) for the core recursive-descent expression parser. I extended a targeted section of Chapter 8's statement-handling concepts (declarations, `var`, blocks) to support statements, and added original grammar for my river-language constructs (`river`, `dam`, `release`, `from`, `output`, `rain`) beyond anything in the textbook.

2. What is the "working folder", and what command(s) compile your parser?

### Your answer

The working folder is the root of the submitted zip; all `.java` files sit flat with no subfolders (no packages are used). Compile with `javac *.java`. Run an example program with `java lox Program1.txt` (or `Program2.txt` / `Program3.txt`), which reads the file, parses it, and prints the resulting parsed syntax tree for each statement.

3. What literal in your language represents a river that gets 10L/s of flow on the first day after 1mm of rainfall?

### Your answer

rain(1, 1, 10)

4. What symbol in your language shows two rivers combine, and is it a "unary", "binary", or "literal"?

### Your answer

`+`, implemented as a Binary expression (Expr.Binary) — it takes two operands (the two flows being combined) with the operator between them, reusing the standard arithmetic addition operator since the domain rule states that flow into a river is the sum of the flows out of its feeding rivers.

5. Does your language include statements, or is it an expression language?

### Your answer

My language includes statements. Declarations (`river`, `dam`, `var`) and control statements (`if`/`else`, `release`, blocks `{ }`) are all statements (Stmt), distinct from the expression grammar (arithmetic, comparisons, `rain(...)` literals, variable references, `from` connections) built on Chapter 6's expression parser.

6. In your language, how long does it take all the water to work through a river system after 1 day of rain?

### Your answer

My language does not enforce a fixed number of days like the exemplar's 10-day window. Each river's flow is described by a `rain(start, spread, magnitude)` literal or a dam's release logic, so how long water takes to fully work through the system depends on the specific `start`/`spread` values chosen along the path, rather than a single fixed constant built into the language.

## Log-book submissions (10)

Which file in the zip are your log-book entries and when did you make them?  Your teacher needs to have seen them during the semester.

### Your answer

My log-book entries are in `logbook.md`, included in the root of this zip. The file was made on 27th september with all the weeks together. each week was logged in with its respective date mentioned in the file 

## Grammar given in the document in Nystrom's notation (20)

Provide the grammar for your language, and how does each of your example programs parse according to it?

### Your answer

program     → declaration* EOF ;

declaration → damDecl
            | varDecl
            | riverDecl
            | statement ;

damDecl     → "dam" IDENTIFIER "(" parameters? ")" block ;
parameters  → IDENTIFIER ( "," IDENTIFIER )* ;

varDecl     → "var" IDENTIFIER ( "=" expression )? ";" ;

riverDecl   → "output"? "river" IDENTIFIER "=" (connection | expression) ";" ;
connection  → IDENTIFIER "from" expression ;

statement   → exprStmt
            | block
            | ifStmt
            | releaseStmt ;

block       → "{" declaration* "}" ;
ifStmt      → "if" "(" expression ")" statement ( "else" statement )? ;
releaseStmt → "release" expression ";" ;
exprStmt    → expression ";" ;

expression  → equality ;
equality    → comparison ( ( "!=" | "==" ) comparison )* ;
comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → ( "!" | "-" ) unary
            | primary ;
primary     → NUMBER | STRING | "true" | "false" | "nil"
            | "(" expression ")"
            | IDENTIFIER
            | flowLiteral ;

flowLiteral → "rain" "(" expression "," expression "," expression ")" ;


**How Program1.txt parses** (`output river googong = rain(1, 1, 10);`):

The line matches `riverDecl`. `"output"?` is matched (present), `"river"` is matched, `IDENTIFIER` matches `googong`, `"="` is matched. To decide between `connection` and `expression` on the right-hand side, the parser checks whether the next token is an identifier followed by `from` — here it is `rain`, a keyword, so it takes the `expression` branch. That expression descends through `equality → comparison → term → factor → unary → primary`, where `primary` recognises the `rain` keyword and matches `flowLiteral → "rain" "(" expression "," expression "," expression ")"`. Each of the three arguments (`1`, `1`, `10`) is itself parsed as a full `expression`, resolving to a `NUMBER` at `primary`. The trailing `";"` closes the `riverDecl`.

**How Program2.txt parses** (three root river declarations, then `output river central_molongolo = googong + jerrabombarra + upper_molongolo;`):

The first three lines each match `riverDecl` with a `rain(...)` `flowLiteral` on the right-hand side, exactly as in example1.txt. The fourth line also matches `riverDecl`, but its right-hand side is `googong + jerrabombarra + upper_molongolo`. This is parsed as a plain `expression`, which descends to `term`, since `term → factor ( ( "-" | "+" ) factor )*`. `term` first parses `googong` as its left-hand `factor` (resolving to an `IDENTIFIER` at `primary`), then matches `"+"`, then parses `jerrabombarra` as the next `factor`, combining them into a `Binary` expression. Because the `term` rule's `*` allows repetition, it then matches a second `"+"` and parses `upper_molongolo`, combining the previous `Binary` result with it into a second, outer `Binary` expression. This demonstrates left-associativity: the expression parses as `(googong + jerrabombarra) + upper_molongolo`, not `googong + (jerrabombarra + upper_molongolo)`.

**How Program3.txt parses** (the full river system, with dams and a `rain(...)` literal inside dam logic):

The three root river declarations parse as in example1.txt. Each `dam` declaration (e.g. `dam dam1(capacity, startingLevel) { ... }`) matches `damDecl → "dam" IDENTIFIER "(" parameters? ")" block`: the name `dam1` matches `IDENTIFIER`, the parameter list `capacity, startingLevel` matches `parameters → IDENTIFIER ( "," IDENTIFIER )*`, and the `{ ... }` body matches `block → "{" declaration* "}"`, which loops over `declaration` to parse the statements inside.

Inside a dam's body, `if (level > capacity) { release inflow + inflow; } else if (level < 50) { release 0; } else { release inflow; }` matches `ifStmt → "if" "(" expression ")" statement ( "else" statement )?`. The condition `level > capacity` parses as an `expression` descending to `comparison`, matching identifiers `level` and `capacity` as `primary` on either side of `">"`. The "then" branch is a `block` containing one `releaseStmt → "release" expression ";"`. The `else` branch is itself another full `statement`, which — because the next token is `if` — recursively matches `ifStmt` again; this is how the `else if` chain is expressed, as a nested `If` inside the outer statement's `else` branch, with no separate grammar rule needed for `else if`.

`river queanbeyan = dam1 from googong;` matches `riverDecl`, but this time the lookahead check (identifier followed by `from`) succeeds, so the right-hand side matches `connection → IDENTIFIER "from" expression`, with `dam1` as the `IDENTIFIER` and `googong` (resolving to an `expression` via `primary`) as the source.

`dam3`'s body demonstrates a `flowLiteral` used inside a `releaseStmt`, rather than only inside a `riverDecl`: `release rain(2, 1, 5);` matches `releaseStmt → "release" expression ";"`, where the `expression` descends through the full precedence chain to `primary`, matching `flowLiteral` exactly as in example1.txt. This shows the same grammar rule (`flowLiteral`) being reused in a different syntactic position, since it is defined at the `primary` level and is therefore valid anywhere an `expression` is expected.

Finally, `output river final_output = dam3 from lower_molongolo;` matches `riverDecl` with `"output"?` present and a `connection` on the right-hand side, identical in structure to the `queanbeyan` declaration above.

## Three example programs (20)

Provide your three example programs here and identify which files in your zip contain them.

### Your answer

My three example programs are included in the zip as `Program1.txt`, `Program2.txt`, and `Program3.txt`.

**Program1.txt** — the simplest possible program: a single root river declared with a `rain(...)` flow literal, marked directly as the system's output.

output river googong = rain(1, 1, 10);

**Program2.txt** — demonstrates combining multiple rivers using `+`: three root rivers, each with their own `rain(...)` flow literal, combine into one output river via a left-associative chain of `Binary` expressions.

river googong = rain(1, 1, 10);
river jerrabombarra = rain(1, 2, 5);
river upper_molongolo = rain(1, 1, 8);
output river central_molongolo = googong + jerrabombarra + upper_molongolo;

**Program3.txt** — a full river system modelled on the Canberra example from the assignment brief: three root rivers, three dams with parameterised behaviour and conditional release logic (including a nested `else if` chain and a `rain(...)` literal used inside dam logic), `from` connections routing flow through each dam, a confluence, and a marked output river.

river googong = rain(1, 1, 10);
river jerrabombarra = rain(1, 2, 6);
river upper_molongolo = rain(1, 1, 8);

dam dam1(capacity, startingLevel) {
if (level > capacity) {
release inflow + inflow;
} else if (level < 50) {
release 0;
} else {
release inflow;
}
}

river queanbeyan = dam1 from googong;
river central_molongolo = queanbeyan + upper_molongolo + jerrabombarra;

dam dam2(capacity, startingLevel) {
if (level > capacity) {
release inflow + inflow;
} else {
release inflow;
}
}

river lower_molongolo = dam2 from central_molongolo;

dam dam3(capacity, startingLevel) {
if (level > capacity) {
release rain(2, 1, 5);
} else if (level < 10) {
release rain(1, 0, 1);
} else {
release inflow;
}
}

output river final_output = dam3 from lower_molongolo;



All three programs have been tested against my parser and confirmed to parse successfully, with their resulting parsed structure printed via `AstPrinter`. Run with: `java lox Program1.txt` (or `Program2.txt` / `Program3.txt`), after compiling with `javac *.java`.


## Parser written in Java based on Lox codebase (20)

Which chapter of the book is your parser based on?  What did you add beyond the Chapter 6 code, and where is that explained?

### Your answer

My parser is based on Chapter 6 ("Parsing Expressions") of "Crafting Interpreters", the full recursive-descent expression grammar (`expression → equality → comparison → term → factor → unary → primary`), the `Parser` class structure (`tokens`, `current`, `match`/`check`/`advance`/`peek`/`previous`/`consume`), and the Chapter 5 AST generation approach (`GenerateAst.java` producing `Expr.java` via the Visitor pattern) are taken directly from the textbook with no structural changes.

Beyond Chapter 6, I added:

- A minimal, targeted subset of Chapter 8 (Statements and State): a `Stmt` AST family (generated with the same `GenerateAst.java` tool, extended to also produce `Stmt.java`), `declaration()`/`statement()` parsing methods, `var` declarations, and top-level program parsing (`parse()` returning `List<Stmt>` instead of a single `Expr`). I deliberately did not implement the rest of Chapter 8 (e.g. `print` statements) since they were not needed for this submission.
- Block statements (`{ ... }`) and `if`/`else` conditional statements — not covered in Chapter 6, added to support dam bodies with conditional release logic.
- All of my river-language-specific grammar and AST nodes, none of which exist in the textbook: `river` declarations (with an optional `output` modifier and `from` connections to dams), `dam` declarations (with parameters and a block body), `release` statements, and `rain(start, spread, magnitude)` flow literals — each with its own new `Expr`/`Stmt` class, parser method, and token type, following the same "one grammar rule → one parsing method" pattern the textbook establishes.
- An extension to `AstPrinter` so that it implements `Stmt.Visitor<String>` alongside `Expr.Visitor<String>` — the textbook's own `AstPrinter` only ever implements `Expr.Visitor<String>`, since statements do not exist yet at that point in the book. I extended it to properly print full parsed programs (declarations, dam bodies, nested control flow) using the same Visitor pattern, rather than a manual type-checking chain.
- Moved the program's entry point into `lox.java`'s existing `run()` method (extending the Chapter 4 `runFile`/`runPrompt`/`run` structure) so that running a program via `java lox <file>` parses it and prints its full syntax tree, directly demonstrating the parser's output.

These additions are explained throughout this document, the full grammar and its correspondence to my parser code is given in the Grammar section above, and my design rationale for each construct is given in the Uniqueness and Creativity section below.

## Uniqueness and Creativity (20)

What did you do beyond the in-class work?  Point your marker to where it lives in your submission.

### Your answer

My language departs from the exemplar and from in-class work in several deliberate ways, all implemented in `Parser.java`, `GenerateAst.java`-generated `Expr.java`/`Stmt.java`, and demonstrated across all three example programs:

- **Keyword-based syntax throughout, rather than symbols.** Where the exemplar uses `<-` for dam connections and `[start~spread]@magnitude` for flow literals, I used the keywords `from` and `rain(start, spread, magnitude)` instead, alongside `river`, `dam`, `release`, and `output`. My language reads closer to structured English than symbolic notation, which was a deliberate stylistic choice I made consistently across every construct rather than mixing symbols and keywords.

- **Dams as parameterised, reusable behaviours from the outset.** The exemplar treats each `dam` as a one-off declaration; parameterising dam behaviour (e.g. `dam dam1(capacity, startingLevel) { ... }`, instantiated differently per river) is explicitly listed in the exemplar's own document as a "possible extension" beyond their base design. I built this as my language's core dam mechanism rather than an optional add-on — see `damDeclaration()` in `Parser.java` and the `dam1`/`dam2`/`dam3` declarations in `Program3.txt`.

- **A `startingLevel` parameter addressing a real limitation in the exemplar's model.** The exemplar's dam `level` always starts at zero, which is unrealistic — a real dam is not empty when a simulation begins. My dams accept a `startingLevel` parameter alongside `capacity`, so a dam can be modelled as already partially or fully full at the start of a simulation, without needing any special-cased logic beyond the interpreter seeding `level` from this parameter once, at day one (a design decision for Submission Two's evaluation, but reflected in the parser's grammar and parameter list now).

- **`if`/`else` conditional logic instead of `when`/`default`.** The exemplar's own documentation describes `when` as "syntactic sugar for if/else"; I used real `if`/`else` statements directly (see `ifStatement()` in `Parser.java`), including natural support for `else if` chains via recursion (a nested `If` statement inside an `else` branch), with no extra grammar rule required — demonstrated in `dam1`'s three-way condition in `Program3.txt`.

- **An explicit `release` statement**, rather than the exemplar's implicit "last expression in the dam body is the output" convention, making a dam's output value an explicit, visible statement rather than an implicit convention.

- **`rain(...)` reused across two different syntactic positions.** The same `flowLiteral` grammar rule and AST node is used both to define a root river's initial flow (`river googong = rain(1, 1, 10);`) and inside a dam's conditional release logic (`release rain(2, 1, 5);` in `dam3`, `Program3.txt`) — demonstrating that defining `flowLiteral` at the `primary` level of the expression grammar makes it valid anywhere an expression is expected, without needing separate grammar rules for each context.

- **Extending the Visitor pattern beyond the textbook.** `AstPrinter` (in `AstPrinter.java`) implements both `Expr.Visitor<String>` and `Stmt.Visitor<String>` — the textbook's own `AstPrinter` only ever implements the former, since statements are introduced in a later chapter. I extended it to properly print full parsed programs, including nested dam bodies and control flow, using the Visitor pattern rather than manual type-checking.

All of the above can be seen running end-to-end via `java lox Program3.txt`, which exercises every one of these constructs together in a single program.
