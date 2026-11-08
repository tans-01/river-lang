# COMP3000 Log Book (submission 2)

## Week 9

Did the IRAT, then asked the teacher some questions I still had from Assignment 1 about the flow syntax:
1. What `plot` means in the exemplar.
2. What it means for the flow syntax to output a 10-element array, what that array represents, and whether I have to model flows that way.
3. Confirmed my understanding of how flows work across multiple days.

Discussed the assignment code with a team member: we looked through each other's code, asked each other questions about it, and talked about what each of us would do next for the assignment.

Brainstormed how to handle the flow literal. It is currently `rain(start, spread, magnitude)`, but I wasn't fully satisfied with it. I had two ideas for changing it that I hadn't settled on yet, so I left it to think about further.

## Week 10

Did not attend class this week because I was busy with other assignments that were due, but I completed the application exercise on my own.

Kept thinking about the flow logic. In week 9 the tutor advised me not to overcomplicate the design, which made me lean towards keeping `rain(start, spread, magnitude)` as it is rather than changing it, since my alternative ideas would have added complexity without much benefit.

Finished reading Chapter 8 (Statements and State). For Submission One I had only read about half of it, just enough to add statements and `var` declarations; this time I read the rest, including environments and scope, which I would need for the interpreter.

## Week 11

Did not attend class this week because I had several other assignments due in the same week, but I completed the application exercise on my own.

The exercise was about dam semantics: a water manager's release decision expressed as an "algorithm" with inputs and an output, and three versions of the same Googong dam (blocks half; blocks half unless rain > 10mm; blocks half unless inflow > 10 L/s, then blocks 75%). The exercise suggested a function-like syntax because a dam's outflow depends on its inflow. This confirmed the direction I'd already taken in Submission One, where dams are declared with parameters and use `if`/`else` with `release`. It also showed me I needed a way for a dam to read today's rainfall, which until now had been implicit; I decided to provide `rainfall` and `inflow` as names that are automatically available inside a dam body. I later wrote these three versions as Program6.txt.

Also worked through the Chapter 10 self-study (functions): traced a Lox program where a function returns another function, and the Church numerals example (`zero`, `one`, `plus`), which helped me understand closures and how a `return` value is passed back from a call. This is the idea I reused for `release` in my dams which i already planned from submission 1.

## Week 12

Did not attend class this week for the same reason (other assignments due), and did not get to the self-study exercises.

Started implementing the interpreter for Submission Two.

From Chapter 8 (Statements and State), I implemented:
- the `Interpreter` running a list of statements instead of a single expression (`interpret(List<Stmt>)`)
- an `Environment` class that stores names and their values (`define` and `get`), with an "Undefined variable" runtime error
- `visitVarStmt` and `visitVariableExpr`, so `var` declarations store a value and later expressions can look it up
- `visitExpressionStmt` and `visitBlockStmt`
- `visitRiverStmt`, which stores each river in the same environment as variables, so rivers can be referred to by name later

I adapted the environment rather than copying the book exactly: my rivers live in one global environment, and I added nested environments only where I needed them (inside dams).

Read Chapter 9 (Control Flow) and implemented about half of what I needed from it: `visitIfStmt`, using the existing `isTruthy` rule from Chapter 7 to decide which branch to run. The rest (connecting `if`/`else` to dam bodies so they run once per day) I finished in week 13.

## Week 13

Finished the interpreter for Submission Two.

**Flows.** Implemented `rain(start, spread, magnitude)` as a 10-day array: each day a fraction 1/(spread+1) of the remaining catchment water reaches the river, and the rest carries over. Rainfall comes from the command line and catchment size is `magnitude`. Arithmetic now works day by day on flows, and a final report prints every river and marks the output.

**Finishing Chapter 9.** Connected `if`/`else` to dam bodies so each dam's rules run once per day.

**From Chapter 10 (Functions).** Dams work like function calls, so I reused the ideas from this chapter:
- a dam declaration is stored in the environment (like `visitFunctionStmt`)
- using a dam (`dam1(100, 50) from googong`) checks the number of arguments matches the number of parameters, then binds them in a new environment (like `LoxFunction.call`)
- `Environment` got a parent (`enclosing`) scope, so a dam body can see outer names
- `release` is implemented as an exception modelled on the book's `Return`, so it stops the dam body and hands back a value

On top of the book, each dam body runs once per day with `inflow`, `level`, `rainfall` and `day` provided automatically, the release is capped at `level + inflow` so a dam can never release more water than it holds, and the level carries over to the next day.

**Debugging.** The parser's dam-connection check was missing brackets, so `rain(` was treated as a dam call; my flow arrays were `Double[]` instead of `double[]`; and my type check used `&&` where it needed `||`, which rejected valid additions.

**Scanner decimal bug.** While testing my example programs, a dam rule using `release inflow * 0.2;` failed with "Expected ';' after release statement". Tracing it back, I found the bug was in my Chapter 4 scanner, not the parser: the check for a decimal point was inside the digit loop, so it only ran when the number had at least two digits before the dot. `10.5` worked, but `0.5` was split into `0`, `.` and `5`. I moved the decimal check after the loop, as in the book, and decimals now scan correctly.

**Example programs.** Program3's dams passed water straight through with their settings, which was correct but didn't show much, so I wrote Program4 (no-flow, half-flow and full-flow dams, a percentage `gate` dam clamped at 100%, and a `greedy` dam that hits the cap) and Program5 (a dam whose release depends on inflow, level, capacity and rainfall, compared at 1 mm and 25 mm), and also wrote program6 which was inspired from week 11 exercise.