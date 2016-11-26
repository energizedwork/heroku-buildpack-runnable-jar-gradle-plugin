ruleset {
    ruleset("rulesets/basic.xml")
    ruleset("rulesets/braces.xml")
    ruleset("rulesets/convention.xml") {
        NoDef {
            enabled = false
        }
        TrailingComma {
            enabled = false
        }
    }
    ruleset("rulesets/design.xml")
    ruleset("rulesets/dry.xml")
    ruleset("rulesets/exceptions.xml") {
        ThrowRuntimeException {
            doNotApplyToClassNames = "HerokuDeploy"
        }
    }
    ruleset("rulesets/formatting.xml") {
        ClassJavadoc {
            enabled = false
        }
        SpaceAroundMapEntryColon {
            characterAfterColonRegex = /\s/
        }
        LineLength {
            length = 160
        }
        SpaceAfterOpeningBrace {
            ignoreEmptyBlock = true
        }
        SpaceBeforeClosingBrace {
            ignoreEmptyBlock = true
        }
    }
    ruleset("rulesets/generic.xml")
    ruleset("rulesets/groovyism.xml")
    ruleset("rulesets/imports.xml") {
        MisorderedStaticImports {
            comesBefore = false
        }
        NoWildcardImports {
            enabled = false
        }
    }
    ruleset("rulesets/logging.xml")
    ruleset("rulesets/naming.xml") {
        MethodName {
            regex = /[a-z]\w*( \w+)*/
        }
        FactoryMethodName {
            enabled = false
        }
    }
    ruleset("rulesets/unnecessary.xml")
    ruleset("rulesets/unused.xml")
}