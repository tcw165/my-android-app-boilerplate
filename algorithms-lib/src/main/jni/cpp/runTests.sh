#!/usr/bin/env sh

# Compile the unit-tests.
source runCompileTests.sh

# TODO: Could we provide text-based UI so that tester could chose what test
# TODO: to test.
# Run the all unit-tests.
./UnitTest -s

# Force to return 0 so that "&&" can be processed.
echo ""

# UnitTest usage:
#   UnitTest [<test name, pattern or tags> ...] [options]
#
# where options are:
#   -?, -h, --help               display usage information
#   -l, --list-tests             list all/matching test cases
#   -t, --list-tags              list all/matching tags
#   -s, --success                include successful tests in output
#   -b, --break                  break into debugger on failure
#   -e, --nothrow                skip exception tests
#   -i, --invisibles             show invisibles (tabs, newlines)
#   -o, --out <filename>         output filename
#   -r, --reporter <name>        reporter to use (defaults to console)
#   -n, --name <name>            suite name
#   -a, --abort                  abort at first failure
#   -x, --abortx <no. failures>  abort after x failures
#   -w, --warn <warning name>    enable warnings
#   -d, --durations <yes|no>     show test durations
#   -f, --input-file <filename>  load test names to run from a file
#   -#, --filenames-as-tags      adds a tag for the filename
#   --list-test-names-only       list all/matching test cases names only
#   --list-reporters             list all reporters
#   --order <decl|lex|rand>      test case order (defaults to decl)
#   --rng-seed <'time'|number>   set a specific seed for random numbers
#   --force-colour               force colourised output (deprecated)
#   --use-colour <yes|no>        should output be colourised
#
# For more detail usage please see the project docs
