# COMP3000 Log Book — Tans

## Week 1
Absent — did not attend.

## Week 2 - (8 august 2026)

Covered the foundational concepts: what makes a language a "little language," the compiler/interpreter pipeline (scanner → parser → static analysis → optimisation → code generation), and the distinction between compilers (translate without running) and interpreters (execute directly).

Worked through the regex application exercise using regexr.com — testing exact-string matches, alternation, repeated characters, and capture groups against a sample script, as a case study for what makes a language "complete."

Only got through about half of the self-study exercises — covered identifying a "little language" I'd used before and classifying the compiler parts (scanner/parser/static analysis/optimisation/code generation), but didn't finish the remaining questions on compiler/interpreter classification and language completeness.

## Week 3 - (14 august 2026)

Started with a simple Lox program that printed a single `"fd 10"` command to confirm the basic approach. Moved on to generating the star shape, which requires `fd 100` and `rt 144` repeated five times, initially wrote this with individual `print` statements for each line.

Attempted to use a `for` loop to generate the repeated lines instead, but was unfamiliar with the loop syntax and could not initially get it to correctly repeat the two statements together. After working through it, arrived at `for (var i = 1; i < 5; i = i + 1) { print "fd 100"; print "rt 144"; }`, which correctly generated the star pattern (with one extra trailing turn that did not affect the resulting shape).

Attempted the dotted-line version, which required a nested loop, an outer loop for the five sides of the star, and an inner loop breaking each `fd 100` into smaller `fd 10` segments with `pu`/`pd` alternating to produce the dashed effect.

Did not complete the self-study exercises for this week.

## Week 4 - (22 august 2026)

I was still catching up on the scanner implementation from earlier in the semester at this point, so my focus this week was on finishing that rather than the flow-literal design task. Did not settle on or contribute a specific literal design during the reporting period this week.

## Week 5 - (27 august 2026)

This week's focus was on how arithmetic expressions form trees, and how the branching structure of a watershed maps onto similar tree-based representations. The task was to work out how a symbol like `+` could describe rivers combining, and to produce a Nystrom-notation grammar extending Lox's parser to cover it.

I tried out a number of different approaches for the water-flow literal this week. One idea I explored was a function-style form, something like `flow(river1) { ... }`, where a river's flow would be defined by a small block of logic rather than a fixed literal. I went back and forth on this and other syntax shapes while trying to figure out how the flow literal itself should look, without landing on anything final. For combining rivers, I preferred `+` over other options considered — it felt like the simplest and most natural fit, since combining flows is essentially just summing them.

I also spent a good part of the week trying to properly understand grammar notation (Nystrom's CFG notation) itself, which was initially confusing — reading the rules on the page didn't fully click for me. After a few iterations and some practice working through examples, it eventually clicked.

## Week 6 - (1 september 2026)

This week's task was to write a parser for the water-flow expression grammar sketched out last week, following chapter 6 of "Crafting Interpreters" as the model.

At the start of the week I was focused on building the general Lox expression parser for my own personal interpreter project (following chapters 4–6), rather than the assignment's parser specifically, getting the scanner and recursive-descent parser working end to end for plain Lox expressions.

For the assignment itself, I came up with a basic idea for what my water-flow grammar could look like, extending the standard expression grammar with a new literal form, roughly:

expression -> term ( "+" term )* ;
term -> flow | "(" expression ")" ;
flow -> NUMBER ;
river_decleration -> "river" IDENTIFIER "=" (expression|connection) "';";

— I had not yet implemented this in code at this point, just worked out the shape of it on paper.

## Weeks 7 – (6 september 2026 -> 27 september 2026)

Over the following weeks, I continued building out my personal Lox interpreter (chapters 4–6: scanner, AST representation with the Visitor pattern via a `GenerateAst.java` code generator, and the full recursive-descent expression parser with proper operator precedence), and began extending a narrow slice of chapter 8 (declarations, `var`, sequencing multiple statements) to support the statement-based structure my river language would need.

I forked my Lox foundation into a dedicated repository for the assignment and designed my river-language grammar from the ground up, deliberately departing from the exemplar's approach in several places while taking inspiration from its overall shape. Key decisions:

- Keyword-based syntax throughout (`river`, `dam`, `release`, `from`, `output`, `rain`) rather than the exemplar's symbol-heavy style (`<-`, `[~]@`), which according to me would come as vague to someone new using the program.
- Dams as parameterised, reusable behaviours from the outset (`dam dam1(capacity, startingLevel) { ... }`), rather than one-off declarations, an idea the exemplar only mentions as a possible extension.
- `if`/`else` conditional logic for dam behaviour instead of the exemplar's `when`/`default` syntax.
- An explicit `release` statement marking a dam's output value, rather than an implicit last-expression convention.
- A `startingLevel` parameter addressing a real limitation I identified in the exemplar's model (their dam `level` always starts at zero, which is unrealistic for a dam that already holds water).
- Kept `+` for combining river flows, since it directly matches the domain's own rule that combining flows is summation.
- Adopted the exemplar's `<-`-style ordering convention for dam connections, but expressed as the keyword `from` instead of a symbol.
- Designed a `rain(start, spread, magnitude)` flow literal, built incrementally alongside block statements, `if`/`else`, and parameterised dam declarations — extending my `GenerateAst.java` generator and recursive-descent parser to support each new construct.

I implemented and tested this grammar end-to-end, extending my `AstPrinter` class to properly implement `Stmt.Visitor<String>` alongside `Expr.Visitor<String>` (an extension beyond the textbook's own `AstPrinter`, which only ever handles expressions), so that running a program prints its full parsed structure — confirming correctness across all constructs. I wrote three example programs of increasing complexity (a single root river; multiple rivers combined via `+`; a full system modelled on the assignment's Canberra example, with multiple dams, `from` connections, and a `rain(...)` literal used inside conditional dam logic), tested each against my parser, and confirmed all parse and print correctly.