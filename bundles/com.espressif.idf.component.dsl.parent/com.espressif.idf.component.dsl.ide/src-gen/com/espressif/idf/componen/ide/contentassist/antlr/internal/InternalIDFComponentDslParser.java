package com.espressif.idf.componen.ide.contentassist.antlr.internal;

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.DFA;
import com.espressif.idf.componen.services.IDFComponentDslGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalIDFComponentDslParser extends AbstractInternalContentAssistParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'description:'", "'version:'", "'url:'", "'#'", "'dependencies:'", "'idf:'", "':'", "'public:'", "'override_path:'", "'rules:'"
    };
    public static final int RULE_STRING=4;
    public static final int RULE_SL_COMMENT=8;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int EOF=-1;
    public static final int RULE_ID=5;
    public static final int RULE_WS=9;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=6;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__20=20;

    // delegates
    // delegators


        public InternalIDFComponentDslParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalIDFComponentDslParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalIDFComponentDslParser.tokenNames; }
    public String getGrammarFileName() { return "InternalIDFComponentDsl.g"; }


    	private IDFComponentDslGrammarAccess grammarAccess;

    	public void setGrammarAccess(IDFComponentDslGrammarAccess grammarAccess) {
    		this.grammarAccess = grammarAccess;
    	}

    	@Override
    	protected Grammar getGrammar() {
    		return grammarAccess.getGrammar();
    	}

    	@Override
    	protected String getValueForTokenName(String tokenName) {
    		return tokenName;
    	}



    // $ANTLR start "entryRuleComponentModel"
    // InternalIDFComponentDsl.g:53:1: entryRuleComponentModel : ruleComponentModel EOF ;
    public final void entryRuleComponentModel() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:54:1: ( ruleComponentModel EOF )
            // InternalIDFComponentDsl.g:55:1: ruleComponentModel EOF
            {
             before(grammarAccess.getComponentModelRule()); 
            pushFollow(FOLLOW_1);
            ruleComponentModel();

            state._fsp--;

             after(grammarAccess.getComponentModelRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleComponentModel"


    // $ANTLR start "ruleComponentModel"
    // InternalIDFComponentDsl.g:62:1: ruleComponentModel : ( ( rule__ComponentModel__ElementsAssignment )* ) ;
    public final void ruleComponentModel() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:66:2: ( ( ( rule__ComponentModel__ElementsAssignment )* ) )
            // InternalIDFComponentDsl.g:67:2: ( ( rule__ComponentModel__ElementsAssignment )* )
            {
            // InternalIDFComponentDsl.g:67:2: ( ( rule__ComponentModel__ElementsAssignment )* )
            // InternalIDFComponentDsl.g:68:3: ( rule__ComponentModel__ElementsAssignment )*
            {
             before(grammarAccess.getComponentModelAccess().getElementsAssignment()); 
            // InternalIDFComponentDsl.g:69:3: ( rule__ComponentModel__ElementsAssignment )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=11 && LA1_0<=15)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:69:4: rule__ComponentModel__ElementsAssignment
            	    {
            	    pushFollow(FOLLOW_3);
            	    rule__ComponentModel__ElementsAssignment();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

             after(grammarAccess.getComponentModelAccess().getElementsAssignment()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleComponentModel"


    // $ANTLR start "entryRuleType"
    // InternalIDFComponentDsl.g:78:1: entryRuleType : ruleType EOF ;
    public final void entryRuleType() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:79:1: ( ruleType EOF )
            // InternalIDFComponentDsl.g:80:1: ruleType EOF
            {
             before(grammarAccess.getTypeRule()); 
            pushFollow(FOLLOW_1);
            ruleType();

            state._fsp--;

             after(grammarAccess.getTypeRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleType"


    // $ANTLR start "ruleType"
    // InternalIDFComponentDsl.g:87:1: ruleType : ( ( rule__Type__Alternatives ) ) ;
    public final void ruleType() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:91:2: ( ( ( rule__Type__Alternatives ) ) )
            // InternalIDFComponentDsl.g:92:2: ( ( rule__Type__Alternatives ) )
            {
            // InternalIDFComponentDsl.g:92:2: ( ( rule__Type__Alternatives ) )
            // InternalIDFComponentDsl.g:93:3: ( rule__Type__Alternatives )
            {
             before(grammarAccess.getTypeAccess().getAlternatives()); 
            // InternalIDFComponentDsl.g:94:3: ( rule__Type__Alternatives )
            // InternalIDFComponentDsl.g:94:4: rule__Type__Alternatives
            {
            pushFollow(FOLLOW_2);
            rule__Type__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getTypeAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleType"


    // $ANTLR start "entryRuleDescription"
    // InternalIDFComponentDsl.g:103:1: entryRuleDescription : ruleDescription EOF ;
    public final void entryRuleDescription() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:104:1: ( ruleDescription EOF )
            // InternalIDFComponentDsl.g:105:1: ruleDescription EOF
            {
             before(grammarAccess.getDescriptionRule()); 
            pushFollow(FOLLOW_1);
            ruleDescription();

            state._fsp--;

             after(grammarAccess.getDescriptionRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleDescription"


    // $ANTLR start "ruleDescription"
    // InternalIDFComponentDsl.g:112:1: ruleDescription : ( ( rule__Description__Group__0 ) ) ;
    public final void ruleDescription() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:116:2: ( ( ( rule__Description__Group__0 ) ) )
            // InternalIDFComponentDsl.g:117:2: ( ( rule__Description__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:117:2: ( ( rule__Description__Group__0 ) )
            // InternalIDFComponentDsl.g:118:3: ( rule__Description__Group__0 )
            {
             before(grammarAccess.getDescriptionAccess().getGroup()); 
            // InternalIDFComponentDsl.g:119:3: ( rule__Description__Group__0 )
            // InternalIDFComponentDsl.g:119:4: rule__Description__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__Description__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getDescriptionAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleDescription"


    // $ANTLR start "entryRuleVersionInfo"
    // InternalIDFComponentDsl.g:128:1: entryRuleVersionInfo : ruleVersionInfo EOF ;
    public final void entryRuleVersionInfo() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:129:1: ( ruleVersionInfo EOF )
            // InternalIDFComponentDsl.g:130:1: ruleVersionInfo EOF
            {
             before(grammarAccess.getVersionInfoRule()); 
            pushFollow(FOLLOW_1);
            ruleVersionInfo();

            state._fsp--;

             after(grammarAccess.getVersionInfoRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleVersionInfo"


    // $ANTLR start "ruleVersionInfo"
    // InternalIDFComponentDsl.g:137:1: ruleVersionInfo : ( ( rule__VersionInfo__Group__0 ) ) ;
    public final void ruleVersionInfo() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:141:2: ( ( ( rule__VersionInfo__Group__0 ) ) )
            // InternalIDFComponentDsl.g:142:2: ( ( rule__VersionInfo__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:142:2: ( ( rule__VersionInfo__Group__0 ) )
            // InternalIDFComponentDsl.g:143:3: ( rule__VersionInfo__Group__0 )
            {
             before(grammarAccess.getVersionInfoAccess().getGroup()); 
            // InternalIDFComponentDsl.g:144:3: ( rule__VersionInfo__Group__0 )
            // InternalIDFComponentDsl.g:144:4: rule__VersionInfo__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__VersionInfo__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getVersionInfoAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleVersionInfo"


    // $ANTLR start "entryRuleURL"
    // InternalIDFComponentDsl.g:153:1: entryRuleURL : ruleURL EOF ;
    public final void entryRuleURL() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:154:1: ( ruleURL EOF )
            // InternalIDFComponentDsl.g:155:1: ruleURL EOF
            {
             before(grammarAccess.getURLRule()); 
            pushFollow(FOLLOW_1);
            ruleURL();

            state._fsp--;

             after(grammarAccess.getURLRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleURL"


    // $ANTLR start "ruleURL"
    // InternalIDFComponentDsl.g:162:1: ruleURL : ( ( rule__URL__Group__0 ) ) ;
    public final void ruleURL() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:166:2: ( ( ( rule__URL__Group__0 ) ) )
            // InternalIDFComponentDsl.g:167:2: ( ( rule__URL__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:167:2: ( ( rule__URL__Group__0 ) )
            // InternalIDFComponentDsl.g:168:3: ( rule__URL__Group__0 )
            {
             before(grammarAccess.getURLAccess().getGroup()); 
            // InternalIDFComponentDsl.g:169:3: ( rule__URL__Group__0 )
            // InternalIDFComponentDsl.g:169:4: rule__URL__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__URL__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getURLAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleURL"


    // $ANTLR start "entryRuleCOMMENT"
    // InternalIDFComponentDsl.g:178:1: entryRuleCOMMENT : ruleCOMMENT EOF ;
    public final void entryRuleCOMMENT() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:179:1: ( ruleCOMMENT EOF )
            // InternalIDFComponentDsl.g:180:1: ruleCOMMENT EOF
            {
             before(grammarAccess.getCOMMENTRule()); 
            pushFollow(FOLLOW_1);
            ruleCOMMENT();

            state._fsp--;

             after(grammarAccess.getCOMMENTRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleCOMMENT"


    // $ANTLR start "ruleCOMMENT"
    // InternalIDFComponentDsl.g:187:1: ruleCOMMENT : ( ( rule__COMMENT__Group__0 ) ) ;
    public final void ruleCOMMENT() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:191:2: ( ( ( rule__COMMENT__Group__0 ) ) )
            // InternalIDFComponentDsl.g:192:2: ( ( rule__COMMENT__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:192:2: ( ( rule__COMMENT__Group__0 ) )
            // InternalIDFComponentDsl.g:193:3: ( rule__COMMENT__Group__0 )
            {
             before(grammarAccess.getCOMMENTAccess().getGroup()); 
            // InternalIDFComponentDsl.g:194:3: ( rule__COMMENT__Group__0 )
            // InternalIDFComponentDsl.g:194:4: rule__COMMENT__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__COMMENT__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getCOMMENTAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleCOMMENT"


    // $ANTLR start "entryRuleDependenciesComp"
    // InternalIDFComponentDsl.g:203:1: entryRuleDependenciesComp : ruleDependenciesComp EOF ;
    public final void entryRuleDependenciesComp() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:204:1: ( ruleDependenciesComp EOF )
            // InternalIDFComponentDsl.g:205:1: ruleDependenciesComp EOF
            {
             before(grammarAccess.getDependenciesCompRule()); 
            pushFollow(FOLLOW_1);
            ruleDependenciesComp();

            state._fsp--;

             after(grammarAccess.getDependenciesCompRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleDependenciesComp"


    // $ANTLR start "ruleDependenciesComp"
    // InternalIDFComponentDsl.g:212:1: ruleDependenciesComp : ( ( rule__DependenciesComp__Group__0 ) ) ;
    public final void ruleDependenciesComp() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:216:2: ( ( ( rule__DependenciesComp__Group__0 ) ) )
            // InternalIDFComponentDsl.g:217:2: ( ( rule__DependenciesComp__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:217:2: ( ( rule__DependenciesComp__Group__0 ) )
            // InternalIDFComponentDsl.g:218:3: ( rule__DependenciesComp__Group__0 )
            {
             before(grammarAccess.getDependenciesCompAccess().getGroup()); 
            // InternalIDFComponentDsl.g:219:3: ( rule__DependenciesComp__Group__0 )
            // InternalIDFComponentDsl.g:219:4: rule__DependenciesComp__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__DependenciesComp__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getDependenciesCompAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleDependenciesComp"


    // $ANTLR start "entryRuleFeature"
    // InternalIDFComponentDsl.g:228:1: entryRuleFeature : ruleFeature EOF ;
    public final void entryRuleFeature() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:229:1: ( ruleFeature EOF )
            // InternalIDFComponentDsl.g:230:1: ruleFeature EOF
            {
             before(grammarAccess.getFeatureRule()); 
            pushFollow(FOLLOW_1);
            ruleFeature();

            state._fsp--;

             after(grammarAccess.getFeatureRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleFeature"


    // $ANTLR start "ruleFeature"
    // InternalIDFComponentDsl.g:237:1: ruleFeature : ( ( rule__Feature__Alternatives ) ) ;
    public final void ruleFeature() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:241:2: ( ( ( rule__Feature__Alternatives ) ) )
            // InternalIDFComponentDsl.g:242:2: ( ( rule__Feature__Alternatives ) )
            {
            // InternalIDFComponentDsl.g:242:2: ( ( rule__Feature__Alternatives ) )
            // InternalIDFComponentDsl.g:243:3: ( rule__Feature__Alternatives )
            {
             before(grammarAccess.getFeatureAccess().getAlternatives()); 
            // InternalIDFComponentDsl.g:244:3: ( rule__Feature__Alternatives )
            // InternalIDFComponentDsl.g:244:4: rule__Feature__Alternatives
            {
            pushFollow(FOLLOW_2);
            rule__Feature__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getFeatureAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleFeature"


    // $ANTLR start "entryRuleIDF"
    // InternalIDFComponentDsl.g:253:1: entryRuleIDF : ruleIDF EOF ;
    public final void entryRuleIDF() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:254:1: ( ruleIDF EOF )
            // InternalIDFComponentDsl.g:255:1: ruleIDF EOF
            {
             before(grammarAccess.getIDFRule()); 
            pushFollow(FOLLOW_1);
            ruleIDF();

            state._fsp--;

             after(grammarAccess.getIDFRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleIDF"


    // $ANTLR start "ruleIDF"
    // InternalIDFComponentDsl.g:262:1: ruleIDF : ( ( rule__IDF__Group__0 ) ) ;
    public final void ruleIDF() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:266:2: ( ( ( rule__IDF__Group__0 ) ) )
            // InternalIDFComponentDsl.g:267:2: ( ( rule__IDF__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:267:2: ( ( rule__IDF__Group__0 ) )
            // InternalIDFComponentDsl.g:268:3: ( rule__IDF__Group__0 )
            {
             before(grammarAccess.getIDFAccess().getGroup()); 
            // InternalIDFComponentDsl.g:269:3: ( rule__IDF__Group__0 )
            // InternalIDFComponentDsl.g:269:4: rule__IDF__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__IDF__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getIDFAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleIDF"


    // $ANTLR start "entryRuleCOMPONENT_NAME"
    // InternalIDFComponentDsl.g:278:1: entryRuleCOMPONENT_NAME : ruleCOMPONENT_NAME EOF ;
    public final void entryRuleCOMPONENT_NAME() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:279:1: ( ruleCOMPONENT_NAME EOF )
            // InternalIDFComponentDsl.g:280:1: ruleCOMPONENT_NAME EOF
            {
             before(grammarAccess.getCOMPONENT_NAMERule()); 
            pushFollow(FOLLOW_1);
            ruleCOMPONENT_NAME();

            state._fsp--;

             after(grammarAccess.getCOMPONENT_NAMERule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleCOMPONENT_NAME"


    // $ANTLR start "ruleCOMPONENT_NAME"
    // InternalIDFComponentDsl.g:287:1: ruleCOMPONENT_NAME : ( ( rule__COMPONENT_NAME__Group__0 ) ) ;
    public final void ruleCOMPONENT_NAME() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:291:2: ( ( ( rule__COMPONENT_NAME__Group__0 ) ) )
            // InternalIDFComponentDsl.g:292:2: ( ( rule__COMPONENT_NAME__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:292:2: ( ( rule__COMPONENT_NAME__Group__0 ) )
            // InternalIDFComponentDsl.g:293:3: ( rule__COMPONENT_NAME__Group__0 )
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getGroup()); 
            // InternalIDFComponentDsl.g:294:3: ( rule__COMPONENT_NAME__Group__0 )
            // InternalIDFComponentDsl.g:294:4: rule__COMPONENT_NAME__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getCOMPONENT_NAMEAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleCOMPONENT_NAME"


    // $ANTLR start "entryRuleCOMPONENT_NAME_FEATURE"
    // InternalIDFComponentDsl.g:303:1: entryRuleCOMPONENT_NAME_FEATURE : ruleCOMPONENT_NAME_FEATURE EOF ;
    public final void entryRuleCOMPONENT_NAME_FEATURE() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:304:1: ( ruleCOMPONENT_NAME_FEATURE EOF )
            // InternalIDFComponentDsl.g:305:1: ruleCOMPONENT_NAME_FEATURE EOF
            {
             before(grammarAccess.getCOMPONENT_NAME_FEATURERule()); 
            pushFollow(FOLLOW_1);
            ruleCOMPONENT_NAME_FEATURE();

            state._fsp--;

             after(grammarAccess.getCOMPONENT_NAME_FEATURERule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleCOMPONENT_NAME_FEATURE"


    // $ANTLR start "ruleCOMPONENT_NAME_FEATURE"
    // InternalIDFComponentDsl.g:312:1: ruleCOMPONENT_NAME_FEATURE : ( ( rule__COMPONENT_NAME_FEATURE__Alternatives ) ) ;
    public final void ruleCOMPONENT_NAME_FEATURE() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:316:2: ( ( ( rule__COMPONENT_NAME_FEATURE__Alternatives ) ) )
            // InternalIDFComponentDsl.g:317:2: ( ( rule__COMPONENT_NAME_FEATURE__Alternatives ) )
            {
            // InternalIDFComponentDsl.g:317:2: ( ( rule__COMPONENT_NAME_FEATURE__Alternatives ) )
            // InternalIDFComponentDsl.g:318:3: ( rule__COMPONENT_NAME_FEATURE__Alternatives )
            {
             before(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getAlternatives()); 
            // InternalIDFComponentDsl.g:319:3: ( rule__COMPONENT_NAME_FEATURE__Alternatives )
            // InternalIDFComponentDsl.g:319:4: rule__COMPONENT_NAME_FEATURE__Alternatives
            {
            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME_FEATURE__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleCOMPONENT_NAME_FEATURE"


    // $ANTLR start "entryRulePUBLIC"
    // InternalIDFComponentDsl.g:328:1: entryRulePUBLIC : rulePUBLIC EOF ;
    public final void entryRulePUBLIC() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:329:1: ( rulePUBLIC EOF )
            // InternalIDFComponentDsl.g:330:1: rulePUBLIC EOF
            {
             before(grammarAccess.getPUBLICRule()); 
            pushFollow(FOLLOW_1);
            rulePUBLIC();

            state._fsp--;

             after(grammarAccess.getPUBLICRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRulePUBLIC"


    // $ANTLR start "rulePUBLIC"
    // InternalIDFComponentDsl.g:337:1: rulePUBLIC : ( ( rule__PUBLIC__Group__0 ) ) ;
    public final void rulePUBLIC() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:341:2: ( ( ( rule__PUBLIC__Group__0 ) ) )
            // InternalIDFComponentDsl.g:342:2: ( ( rule__PUBLIC__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:342:2: ( ( rule__PUBLIC__Group__0 ) )
            // InternalIDFComponentDsl.g:343:3: ( rule__PUBLIC__Group__0 )
            {
             before(grammarAccess.getPUBLICAccess().getGroup()); 
            // InternalIDFComponentDsl.g:344:3: ( rule__PUBLIC__Group__0 )
            // InternalIDFComponentDsl.g:344:4: rule__PUBLIC__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__PUBLIC__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getPUBLICAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rulePUBLIC"


    // $ANTLR start "entryRuleOVERRIDE_PATH"
    // InternalIDFComponentDsl.g:353:1: entryRuleOVERRIDE_PATH : ruleOVERRIDE_PATH EOF ;
    public final void entryRuleOVERRIDE_PATH() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:354:1: ( ruleOVERRIDE_PATH EOF )
            // InternalIDFComponentDsl.g:355:1: ruleOVERRIDE_PATH EOF
            {
             before(grammarAccess.getOVERRIDE_PATHRule()); 
            pushFollow(FOLLOW_1);
            ruleOVERRIDE_PATH();

            state._fsp--;

             after(grammarAccess.getOVERRIDE_PATHRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleOVERRIDE_PATH"


    // $ANTLR start "ruleOVERRIDE_PATH"
    // InternalIDFComponentDsl.g:362:1: ruleOVERRIDE_PATH : ( ( rule__OVERRIDE_PATH__Group__0 ) ) ;
    public final void ruleOVERRIDE_PATH() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:366:2: ( ( ( rule__OVERRIDE_PATH__Group__0 ) ) )
            // InternalIDFComponentDsl.g:367:2: ( ( rule__OVERRIDE_PATH__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:367:2: ( ( rule__OVERRIDE_PATH__Group__0 ) )
            // InternalIDFComponentDsl.g:368:3: ( rule__OVERRIDE_PATH__Group__0 )
            {
             before(grammarAccess.getOVERRIDE_PATHAccess().getGroup()); 
            // InternalIDFComponentDsl.g:369:3: ( rule__OVERRIDE_PATH__Group__0 )
            // InternalIDFComponentDsl.g:369:4: rule__OVERRIDE_PATH__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__OVERRIDE_PATH__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getOVERRIDE_PATHAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleOVERRIDE_PATH"


    // $ANTLR start "entryRuleRULES"
    // InternalIDFComponentDsl.g:378:1: entryRuleRULES : ruleRULES EOF ;
    public final void entryRuleRULES() throws RecognitionException {
        try {
            // InternalIDFComponentDsl.g:379:1: ( ruleRULES EOF )
            // InternalIDFComponentDsl.g:380:1: ruleRULES EOF
            {
             before(grammarAccess.getRULESRule()); 
            pushFollow(FOLLOW_1);
            ruleRULES();

            state._fsp--;

             after(grammarAccess.getRULESRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleRULES"


    // $ANTLR start "ruleRULES"
    // InternalIDFComponentDsl.g:387:1: ruleRULES : ( ( rule__RULES__Group__0 ) ) ;
    public final void ruleRULES() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:391:2: ( ( ( rule__RULES__Group__0 ) ) )
            // InternalIDFComponentDsl.g:392:2: ( ( rule__RULES__Group__0 ) )
            {
            // InternalIDFComponentDsl.g:392:2: ( ( rule__RULES__Group__0 ) )
            // InternalIDFComponentDsl.g:393:3: ( rule__RULES__Group__0 )
            {
             before(grammarAccess.getRULESAccess().getGroup()); 
            // InternalIDFComponentDsl.g:394:3: ( rule__RULES__Group__0 )
            // InternalIDFComponentDsl.g:394:4: rule__RULES__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__RULES__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getRULESAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleRULES"


    // $ANTLR start "rule__Type__Alternatives"
    // InternalIDFComponentDsl.g:402:1: rule__Type__Alternatives : ( ( ruleDescription ) | ( ruleVersionInfo ) | ( ruleDependenciesComp ) | ( ruleURL ) | ( ruleCOMMENT ) );
    public final void rule__Type__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:406:1: ( ( ruleDescription ) | ( ruleVersionInfo ) | ( ruleDependenciesComp ) | ( ruleURL ) | ( ruleCOMMENT ) )
            int alt2=5;
            switch ( input.LA(1) ) {
            case 11:
                {
                alt2=1;
                }
                break;
            case 12:
                {
                alt2=2;
                }
                break;
            case 15:
                {
                alt2=3;
                }
                break;
            case 13:
                {
                alt2=4;
                }
                break;
            case 14:
                {
                alt2=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // InternalIDFComponentDsl.g:407:2: ( ruleDescription )
                    {
                    // InternalIDFComponentDsl.g:407:2: ( ruleDescription )
                    // InternalIDFComponentDsl.g:408:3: ruleDescription
                    {
                     before(grammarAccess.getTypeAccess().getDescriptionParserRuleCall_0()); 
                    pushFollow(FOLLOW_2);
                    ruleDescription();

                    state._fsp--;

                     after(grammarAccess.getTypeAccess().getDescriptionParserRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:413:2: ( ruleVersionInfo )
                    {
                    // InternalIDFComponentDsl.g:413:2: ( ruleVersionInfo )
                    // InternalIDFComponentDsl.g:414:3: ruleVersionInfo
                    {
                     before(grammarAccess.getTypeAccess().getVersionInfoParserRuleCall_1()); 
                    pushFollow(FOLLOW_2);
                    ruleVersionInfo();

                    state._fsp--;

                     after(grammarAccess.getTypeAccess().getVersionInfoParserRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // InternalIDFComponentDsl.g:419:2: ( ruleDependenciesComp )
                    {
                    // InternalIDFComponentDsl.g:419:2: ( ruleDependenciesComp )
                    // InternalIDFComponentDsl.g:420:3: ruleDependenciesComp
                    {
                     before(grammarAccess.getTypeAccess().getDependenciesCompParserRuleCall_2()); 
                    pushFollow(FOLLOW_2);
                    ruleDependenciesComp();

                    state._fsp--;

                     after(grammarAccess.getTypeAccess().getDependenciesCompParserRuleCall_2()); 

                    }


                    }
                    break;
                case 4 :
                    // InternalIDFComponentDsl.g:425:2: ( ruleURL )
                    {
                    // InternalIDFComponentDsl.g:425:2: ( ruleURL )
                    // InternalIDFComponentDsl.g:426:3: ruleURL
                    {
                     before(grammarAccess.getTypeAccess().getURLParserRuleCall_3()); 
                    pushFollow(FOLLOW_2);
                    ruleURL();

                    state._fsp--;

                     after(grammarAccess.getTypeAccess().getURLParserRuleCall_3()); 

                    }


                    }
                    break;
                case 5 :
                    // InternalIDFComponentDsl.g:431:2: ( ruleCOMMENT )
                    {
                    // InternalIDFComponentDsl.g:431:2: ( ruleCOMMENT )
                    // InternalIDFComponentDsl.g:432:3: ruleCOMMENT
                    {
                     before(grammarAccess.getTypeAccess().getCOMMENTParserRuleCall_4()); 
                    pushFollow(FOLLOW_2);
                    ruleCOMMENT();

                    state._fsp--;

                     after(grammarAccess.getTypeAccess().getCOMMENTParserRuleCall_4()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Type__Alternatives"


    // $ANTLR start "rule__Feature__Alternatives"
    // InternalIDFComponentDsl.g:441:1: rule__Feature__Alternatives : ( ( ruleIDF ) | ( ruleOVERRIDE_PATH ) | ( ruleCOMPONENT_NAME ) );
    public final void rule__Feature__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:445:1: ( ( ruleIDF ) | ( ruleOVERRIDE_PATH ) | ( ruleCOMPONENT_NAME ) )
            int alt3=3;
            switch ( input.LA(1) ) {
            case 16:
                {
                alt3=1;
                }
                break;
            case 19:
                {
                alt3=2;
                }
                break;
            case RULE_ID:
                {
                alt3=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // InternalIDFComponentDsl.g:446:2: ( ruleIDF )
                    {
                    // InternalIDFComponentDsl.g:446:2: ( ruleIDF )
                    // InternalIDFComponentDsl.g:447:3: ruleIDF
                    {
                     before(grammarAccess.getFeatureAccess().getIDFParserRuleCall_0()); 
                    pushFollow(FOLLOW_2);
                    ruleIDF();

                    state._fsp--;

                     after(grammarAccess.getFeatureAccess().getIDFParserRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:452:2: ( ruleOVERRIDE_PATH )
                    {
                    // InternalIDFComponentDsl.g:452:2: ( ruleOVERRIDE_PATH )
                    // InternalIDFComponentDsl.g:453:3: ruleOVERRIDE_PATH
                    {
                     before(grammarAccess.getFeatureAccess().getOVERRIDE_PATHParserRuleCall_1()); 
                    pushFollow(FOLLOW_2);
                    ruleOVERRIDE_PATH();

                    state._fsp--;

                     after(grammarAccess.getFeatureAccess().getOVERRIDE_PATHParserRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // InternalIDFComponentDsl.g:458:2: ( ruleCOMPONENT_NAME )
                    {
                    // InternalIDFComponentDsl.g:458:2: ( ruleCOMPONENT_NAME )
                    // InternalIDFComponentDsl.g:459:3: ruleCOMPONENT_NAME
                    {
                     before(grammarAccess.getFeatureAccess().getCOMPONENT_NAMEParserRuleCall_2()); 
                    pushFollow(FOLLOW_2);
                    ruleCOMPONENT_NAME();

                    state._fsp--;

                     after(grammarAccess.getFeatureAccess().getCOMPONENT_NAMEParserRuleCall_2()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Feature__Alternatives"


    // $ANTLR start "rule__COMPONENT_NAME_FEATURE__Alternatives"
    // InternalIDFComponentDsl.g:468:1: rule__COMPONENT_NAME_FEATURE__Alternatives : ( ( ruleRULES ) | ( rulePUBLIC ) );
    public final void rule__COMPONENT_NAME_FEATURE__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:472:1: ( ( ruleRULES ) | ( rulePUBLIC ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==20) ) {
                alt4=1;
            }
            else if ( (LA4_0==18) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // InternalIDFComponentDsl.g:473:2: ( ruleRULES )
                    {
                    // InternalIDFComponentDsl.g:473:2: ( ruleRULES )
                    // InternalIDFComponentDsl.g:474:3: ruleRULES
                    {
                     before(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getRULESParserRuleCall_0()); 
                    pushFollow(FOLLOW_2);
                    ruleRULES();

                    state._fsp--;

                     after(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getRULESParserRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:479:2: ( rulePUBLIC )
                    {
                    // InternalIDFComponentDsl.g:479:2: ( rulePUBLIC )
                    // InternalIDFComponentDsl.g:480:3: rulePUBLIC
                    {
                     before(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getPUBLICParserRuleCall_1()); 
                    pushFollow(FOLLOW_2);
                    rulePUBLIC();

                    state._fsp--;

                     after(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getPUBLICParserRuleCall_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME_FEATURE__Alternatives"


    // $ANTLR start "rule__Description__Group__0"
    // InternalIDFComponentDsl.g:489:1: rule__Description__Group__0 : rule__Description__Group__0__Impl rule__Description__Group__1 ;
    public final void rule__Description__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:493:1: ( rule__Description__Group__0__Impl rule__Description__Group__1 )
            // InternalIDFComponentDsl.g:494:2: rule__Description__Group__0__Impl rule__Description__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__Description__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Description__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Description__Group__0"


    // $ANTLR start "rule__Description__Group__0__Impl"
    // InternalIDFComponentDsl.g:501:1: rule__Description__Group__0__Impl : ( 'description:' ) ;
    public final void rule__Description__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:505:1: ( ( 'description:' ) )
            // InternalIDFComponentDsl.g:506:1: ( 'description:' )
            {
            // InternalIDFComponentDsl.g:506:1: ( 'description:' )
            // InternalIDFComponentDsl.g:507:2: 'description:'
            {
             before(grammarAccess.getDescriptionAccess().getDescriptionKeyword_0()); 
            match(input,11,FOLLOW_2); 
             after(grammarAccess.getDescriptionAccess().getDescriptionKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Description__Group__0__Impl"


    // $ANTLR start "rule__Description__Group__1"
    // InternalIDFComponentDsl.g:516:1: rule__Description__Group__1 : rule__Description__Group__1__Impl ;
    public final void rule__Description__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:520:1: ( rule__Description__Group__1__Impl )
            // InternalIDFComponentDsl.g:521:2: rule__Description__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__Description__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Description__Group__1"


    // $ANTLR start "rule__Description__Group__1__Impl"
    // InternalIDFComponentDsl.g:527:1: rule__Description__Group__1__Impl : ( ( rule__Description__NameAssignment_1 ) ) ;
    public final void rule__Description__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:531:1: ( ( ( rule__Description__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:532:1: ( ( rule__Description__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:532:1: ( ( rule__Description__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:533:2: ( rule__Description__NameAssignment_1 )
            {
             before(grammarAccess.getDescriptionAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:534:2: ( rule__Description__NameAssignment_1 )
            // InternalIDFComponentDsl.g:534:3: rule__Description__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__Description__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getDescriptionAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Description__Group__1__Impl"


    // $ANTLR start "rule__VersionInfo__Group__0"
    // InternalIDFComponentDsl.g:543:1: rule__VersionInfo__Group__0 : rule__VersionInfo__Group__0__Impl rule__VersionInfo__Group__1 ;
    public final void rule__VersionInfo__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:547:1: ( rule__VersionInfo__Group__0__Impl rule__VersionInfo__Group__1 )
            // InternalIDFComponentDsl.g:548:2: rule__VersionInfo__Group__0__Impl rule__VersionInfo__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__VersionInfo__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__VersionInfo__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__VersionInfo__Group__0"


    // $ANTLR start "rule__VersionInfo__Group__0__Impl"
    // InternalIDFComponentDsl.g:555:1: rule__VersionInfo__Group__0__Impl : ( 'version:' ) ;
    public final void rule__VersionInfo__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:559:1: ( ( 'version:' ) )
            // InternalIDFComponentDsl.g:560:1: ( 'version:' )
            {
            // InternalIDFComponentDsl.g:560:1: ( 'version:' )
            // InternalIDFComponentDsl.g:561:2: 'version:'
            {
             before(grammarAccess.getVersionInfoAccess().getVersionKeyword_0()); 
            match(input,12,FOLLOW_2); 
             after(grammarAccess.getVersionInfoAccess().getVersionKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__VersionInfo__Group__0__Impl"


    // $ANTLR start "rule__VersionInfo__Group__1"
    // InternalIDFComponentDsl.g:570:1: rule__VersionInfo__Group__1 : rule__VersionInfo__Group__1__Impl ;
    public final void rule__VersionInfo__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:574:1: ( rule__VersionInfo__Group__1__Impl )
            // InternalIDFComponentDsl.g:575:2: rule__VersionInfo__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__VersionInfo__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__VersionInfo__Group__1"


    // $ANTLR start "rule__VersionInfo__Group__1__Impl"
    // InternalIDFComponentDsl.g:581:1: rule__VersionInfo__Group__1__Impl : ( ( rule__VersionInfo__NameAssignment_1 ) ) ;
    public final void rule__VersionInfo__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:585:1: ( ( ( rule__VersionInfo__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:586:1: ( ( rule__VersionInfo__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:586:1: ( ( rule__VersionInfo__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:587:2: ( rule__VersionInfo__NameAssignment_1 )
            {
             before(grammarAccess.getVersionInfoAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:588:2: ( rule__VersionInfo__NameAssignment_1 )
            // InternalIDFComponentDsl.g:588:3: rule__VersionInfo__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__VersionInfo__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getVersionInfoAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__VersionInfo__Group__1__Impl"


    // $ANTLR start "rule__URL__Group__0"
    // InternalIDFComponentDsl.g:597:1: rule__URL__Group__0 : rule__URL__Group__0__Impl rule__URL__Group__1 ;
    public final void rule__URL__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:601:1: ( rule__URL__Group__0__Impl rule__URL__Group__1 )
            // InternalIDFComponentDsl.g:602:2: rule__URL__Group__0__Impl rule__URL__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__URL__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__URL__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__URL__Group__0"


    // $ANTLR start "rule__URL__Group__0__Impl"
    // InternalIDFComponentDsl.g:609:1: rule__URL__Group__0__Impl : ( 'url:' ) ;
    public final void rule__URL__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:613:1: ( ( 'url:' ) )
            // InternalIDFComponentDsl.g:614:1: ( 'url:' )
            {
            // InternalIDFComponentDsl.g:614:1: ( 'url:' )
            // InternalIDFComponentDsl.g:615:2: 'url:'
            {
             before(grammarAccess.getURLAccess().getUrlKeyword_0()); 
            match(input,13,FOLLOW_2); 
             after(grammarAccess.getURLAccess().getUrlKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__URL__Group__0__Impl"


    // $ANTLR start "rule__URL__Group__1"
    // InternalIDFComponentDsl.g:624:1: rule__URL__Group__1 : rule__URL__Group__1__Impl ;
    public final void rule__URL__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:628:1: ( rule__URL__Group__1__Impl )
            // InternalIDFComponentDsl.g:629:2: rule__URL__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__URL__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__URL__Group__1"


    // $ANTLR start "rule__URL__Group__1__Impl"
    // InternalIDFComponentDsl.g:635:1: rule__URL__Group__1__Impl : ( ( rule__URL__NameAssignment_1 ) ) ;
    public final void rule__URL__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:639:1: ( ( ( rule__URL__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:640:1: ( ( rule__URL__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:640:1: ( ( rule__URL__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:641:2: ( rule__URL__NameAssignment_1 )
            {
             before(grammarAccess.getURLAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:642:2: ( rule__URL__NameAssignment_1 )
            // InternalIDFComponentDsl.g:642:3: rule__URL__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__URL__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getURLAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__URL__Group__1__Impl"


    // $ANTLR start "rule__COMMENT__Group__0"
    // InternalIDFComponentDsl.g:651:1: rule__COMMENT__Group__0 : rule__COMMENT__Group__0__Impl rule__COMMENT__Group__1 ;
    public final void rule__COMMENT__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:655:1: ( rule__COMMENT__Group__0__Impl rule__COMMENT__Group__1 )
            // InternalIDFComponentDsl.g:656:2: rule__COMMENT__Group__0__Impl rule__COMMENT__Group__1
            {
            pushFollow(FOLLOW_5);
            rule__COMMENT__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__COMMENT__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMMENT__Group__0"


    // $ANTLR start "rule__COMMENT__Group__0__Impl"
    // InternalIDFComponentDsl.g:663:1: rule__COMMENT__Group__0__Impl : ( '#' ) ;
    public final void rule__COMMENT__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:667:1: ( ( '#' ) )
            // InternalIDFComponentDsl.g:668:1: ( '#' )
            {
            // InternalIDFComponentDsl.g:668:1: ( '#' )
            // InternalIDFComponentDsl.g:669:2: '#'
            {
             before(grammarAccess.getCOMMENTAccess().getNumberSignKeyword_0()); 
            match(input,14,FOLLOW_2); 
             after(grammarAccess.getCOMMENTAccess().getNumberSignKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMMENT__Group__0__Impl"


    // $ANTLR start "rule__COMMENT__Group__1"
    // InternalIDFComponentDsl.g:678:1: rule__COMMENT__Group__1 : rule__COMMENT__Group__1__Impl ;
    public final void rule__COMMENT__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:682:1: ( rule__COMMENT__Group__1__Impl )
            // InternalIDFComponentDsl.g:683:2: rule__COMMENT__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__COMMENT__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMMENT__Group__1"


    // $ANTLR start "rule__COMMENT__Group__1__Impl"
    // InternalIDFComponentDsl.g:689:1: rule__COMMENT__Group__1__Impl : ( ( rule__COMMENT__NameAssignment_1 )* ) ;
    public final void rule__COMMENT__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:693:1: ( ( ( rule__COMMENT__NameAssignment_1 )* ) )
            // InternalIDFComponentDsl.g:694:1: ( ( rule__COMMENT__NameAssignment_1 )* )
            {
            // InternalIDFComponentDsl.g:694:1: ( ( rule__COMMENT__NameAssignment_1 )* )
            // InternalIDFComponentDsl.g:695:2: ( rule__COMMENT__NameAssignment_1 )*
            {
             before(grammarAccess.getCOMMENTAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:696:2: ( rule__COMMENT__NameAssignment_1 )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==RULE_ID) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:696:3: rule__COMMENT__NameAssignment_1
            	    {
            	    pushFollow(FOLLOW_6);
            	    rule__COMMENT__NameAssignment_1();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

             after(grammarAccess.getCOMMENTAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMMENT__Group__1__Impl"


    // $ANTLR start "rule__DependenciesComp__Group__0"
    // InternalIDFComponentDsl.g:705:1: rule__DependenciesComp__Group__0 : rule__DependenciesComp__Group__0__Impl rule__DependenciesComp__Group__1 ;
    public final void rule__DependenciesComp__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:709:1: ( rule__DependenciesComp__Group__0__Impl rule__DependenciesComp__Group__1 )
            // InternalIDFComponentDsl.g:710:2: rule__DependenciesComp__Group__0__Impl rule__DependenciesComp__Group__1
            {
            pushFollow(FOLLOW_7);
            rule__DependenciesComp__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__DependenciesComp__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__DependenciesComp__Group__0"


    // $ANTLR start "rule__DependenciesComp__Group__0__Impl"
    // InternalIDFComponentDsl.g:717:1: rule__DependenciesComp__Group__0__Impl : ( 'dependencies:' ) ;
    public final void rule__DependenciesComp__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:721:1: ( ( 'dependencies:' ) )
            // InternalIDFComponentDsl.g:722:1: ( 'dependencies:' )
            {
            // InternalIDFComponentDsl.g:722:1: ( 'dependencies:' )
            // InternalIDFComponentDsl.g:723:2: 'dependencies:'
            {
             before(grammarAccess.getDependenciesCompAccess().getDependenciesKeyword_0()); 
            match(input,15,FOLLOW_2); 
             after(grammarAccess.getDependenciesCompAccess().getDependenciesKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__DependenciesComp__Group__0__Impl"


    // $ANTLR start "rule__DependenciesComp__Group__1"
    // InternalIDFComponentDsl.g:732:1: rule__DependenciesComp__Group__1 : rule__DependenciesComp__Group__1__Impl ;
    public final void rule__DependenciesComp__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:736:1: ( rule__DependenciesComp__Group__1__Impl )
            // InternalIDFComponentDsl.g:737:2: rule__DependenciesComp__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__DependenciesComp__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__DependenciesComp__Group__1"


    // $ANTLR start "rule__DependenciesComp__Group__1__Impl"
    // InternalIDFComponentDsl.g:743:1: rule__DependenciesComp__Group__1__Impl : ( ( rule__DependenciesComp__FeaturesAssignment_1 )* ) ;
    public final void rule__DependenciesComp__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:747:1: ( ( ( rule__DependenciesComp__FeaturesAssignment_1 )* ) )
            // InternalIDFComponentDsl.g:748:1: ( ( rule__DependenciesComp__FeaturesAssignment_1 )* )
            {
            // InternalIDFComponentDsl.g:748:1: ( ( rule__DependenciesComp__FeaturesAssignment_1 )* )
            // InternalIDFComponentDsl.g:749:2: ( rule__DependenciesComp__FeaturesAssignment_1 )*
            {
             before(grammarAccess.getDependenciesCompAccess().getFeaturesAssignment_1()); 
            // InternalIDFComponentDsl.g:750:2: ( rule__DependenciesComp__FeaturesAssignment_1 )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==RULE_ID||LA6_0==16||LA6_0==19) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:750:3: rule__DependenciesComp__FeaturesAssignment_1
            	    {
            	    pushFollow(FOLLOW_8);
            	    rule__DependenciesComp__FeaturesAssignment_1();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

             after(grammarAccess.getDependenciesCompAccess().getFeaturesAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__DependenciesComp__Group__1__Impl"


    // $ANTLR start "rule__IDF__Group__0"
    // InternalIDFComponentDsl.g:759:1: rule__IDF__Group__0 : rule__IDF__Group__0__Impl rule__IDF__Group__1 ;
    public final void rule__IDF__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:763:1: ( rule__IDF__Group__0__Impl rule__IDF__Group__1 )
            // InternalIDFComponentDsl.g:764:2: rule__IDF__Group__0__Impl rule__IDF__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__IDF__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__IDF__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__IDF__Group__0"


    // $ANTLR start "rule__IDF__Group__0__Impl"
    // InternalIDFComponentDsl.g:771:1: rule__IDF__Group__0__Impl : ( 'idf:' ) ;
    public final void rule__IDF__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:775:1: ( ( 'idf:' ) )
            // InternalIDFComponentDsl.g:776:1: ( 'idf:' )
            {
            // InternalIDFComponentDsl.g:776:1: ( 'idf:' )
            // InternalIDFComponentDsl.g:777:2: 'idf:'
            {
             before(grammarAccess.getIDFAccess().getIdfKeyword_0()); 
            match(input,16,FOLLOW_2); 
             after(grammarAccess.getIDFAccess().getIdfKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__IDF__Group__0__Impl"


    // $ANTLR start "rule__IDF__Group__1"
    // InternalIDFComponentDsl.g:786:1: rule__IDF__Group__1 : rule__IDF__Group__1__Impl ;
    public final void rule__IDF__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:790:1: ( rule__IDF__Group__1__Impl )
            // InternalIDFComponentDsl.g:791:2: rule__IDF__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__IDF__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__IDF__Group__1"


    // $ANTLR start "rule__IDF__Group__1__Impl"
    // InternalIDFComponentDsl.g:797:1: rule__IDF__Group__1__Impl : ( ( rule__IDF__NameAssignment_1 ) ) ;
    public final void rule__IDF__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:801:1: ( ( ( rule__IDF__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:802:1: ( ( rule__IDF__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:802:1: ( ( rule__IDF__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:803:2: ( rule__IDF__NameAssignment_1 )
            {
             before(grammarAccess.getIDFAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:804:2: ( rule__IDF__NameAssignment_1 )
            // InternalIDFComponentDsl.g:804:3: rule__IDF__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__IDF__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getIDFAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__IDF__Group__1__Impl"


    // $ANTLR start "rule__COMPONENT_NAME__Group__0"
    // InternalIDFComponentDsl.g:813:1: rule__COMPONENT_NAME__Group__0 : rule__COMPONENT_NAME__Group__0__Impl rule__COMPONENT_NAME__Group__1 ;
    public final void rule__COMPONENT_NAME__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:817:1: ( rule__COMPONENT_NAME__Group__0__Impl rule__COMPONENT_NAME__Group__1 )
            // InternalIDFComponentDsl.g:818:2: rule__COMPONENT_NAME__Group__0__Impl rule__COMPONENT_NAME__Group__1
            {
            pushFollow(FOLLOW_9);
            rule__COMPONENT_NAME__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__0"


    // $ANTLR start "rule__COMPONENT_NAME__Group__0__Impl"
    // InternalIDFComponentDsl.g:825:1: rule__COMPONENT_NAME__Group__0__Impl : ( ( rule__COMPONENT_NAME__NameAssignment_0 ) ) ;
    public final void rule__COMPONENT_NAME__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:829:1: ( ( ( rule__COMPONENT_NAME__NameAssignment_0 ) ) )
            // InternalIDFComponentDsl.g:830:1: ( ( rule__COMPONENT_NAME__NameAssignment_0 ) )
            {
            // InternalIDFComponentDsl.g:830:1: ( ( rule__COMPONENT_NAME__NameAssignment_0 ) )
            // InternalIDFComponentDsl.g:831:2: ( rule__COMPONENT_NAME__NameAssignment_0 )
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getNameAssignment_0()); 
            // InternalIDFComponentDsl.g:832:2: ( rule__COMPONENT_NAME__NameAssignment_0 )
            // InternalIDFComponentDsl.g:832:3: rule__COMPONENT_NAME__NameAssignment_0
            {
            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME__NameAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getCOMPONENT_NAMEAccess().getNameAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__0__Impl"


    // $ANTLR start "rule__COMPONENT_NAME__Group__1"
    // InternalIDFComponentDsl.g:840:1: rule__COMPONENT_NAME__Group__1 : rule__COMPONENT_NAME__Group__1__Impl rule__COMPONENT_NAME__Group__2 ;
    public final void rule__COMPONENT_NAME__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:844:1: ( rule__COMPONENT_NAME__Group__1__Impl rule__COMPONENT_NAME__Group__2 )
            // InternalIDFComponentDsl.g:845:2: rule__COMPONENT_NAME__Group__1__Impl rule__COMPONENT_NAME__Group__2
            {
            pushFollow(FOLLOW_10);
            rule__COMPONENT_NAME__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__1"


    // $ANTLR start "rule__COMPONENT_NAME__Group__1__Impl"
    // InternalIDFComponentDsl.g:852:1: rule__COMPONENT_NAME__Group__1__Impl : ( ':' ) ;
    public final void rule__COMPONENT_NAME__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:856:1: ( ( ':' ) )
            // InternalIDFComponentDsl.g:857:1: ( ':' )
            {
            // InternalIDFComponentDsl.g:857:1: ( ':' )
            // InternalIDFComponentDsl.g:858:2: ':'
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getColonKeyword_1()); 
            match(input,17,FOLLOW_2); 
             after(grammarAccess.getCOMPONENT_NAMEAccess().getColonKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__1__Impl"


    // $ANTLR start "rule__COMPONENT_NAME__Group__2"
    // InternalIDFComponentDsl.g:867:1: rule__COMPONENT_NAME__Group__2 : rule__COMPONENT_NAME__Group__2__Impl ;
    public final void rule__COMPONENT_NAME__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:871:1: ( rule__COMPONENT_NAME__Group__2__Impl )
            // InternalIDFComponentDsl.g:872:2: rule__COMPONENT_NAME__Group__2__Impl
            {
            pushFollow(FOLLOW_2);
            rule__COMPONENT_NAME__Group__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__2"


    // $ANTLR start "rule__COMPONENT_NAME__Group__2__Impl"
    // InternalIDFComponentDsl.g:878:1: rule__COMPONENT_NAME__Group__2__Impl : ( ( rule__COMPONENT_NAME__FeaturesAssignment_2 )* ) ;
    public final void rule__COMPONENT_NAME__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:882:1: ( ( ( rule__COMPONENT_NAME__FeaturesAssignment_2 )* ) )
            // InternalIDFComponentDsl.g:883:1: ( ( rule__COMPONENT_NAME__FeaturesAssignment_2 )* )
            {
            // InternalIDFComponentDsl.g:883:1: ( ( rule__COMPONENT_NAME__FeaturesAssignment_2 )* )
            // InternalIDFComponentDsl.g:884:2: ( rule__COMPONENT_NAME__FeaturesAssignment_2 )*
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getFeaturesAssignment_2()); 
            // InternalIDFComponentDsl.g:885:2: ( rule__COMPONENT_NAME__FeaturesAssignment_2 )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==18||LA7_0==20) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:885:3: rule__COMPONENT_NAME__FeaturesAssignment_2
            	    {
            	    pushFollow(FOLLOW_11);
            	    rule__COMPONENT_NAME__FeaturesAssignment_2();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

             after(grammarAccess.getCOMPONENT_NAMEAccess().getFeaturesAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__Group__2__Impl"


    // $ANTLR start "rule__PUBLIC__Group__0"
    // InternalIDFComponentDsl.g:894:1: rule__PUBLIC__Group__0 : rule__PUBLIC__Group__0__Impl rule__PUBLIC__Group__1 ;
    public final void rule__PUBLIC__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:898:1: ( rule__PUBLIC__Group__0__Impl rule__PUBLIC__Group__1 )
            // InternalIDFComponentDsl.g:899:2: rule__PUBLIC__Group__0__Impl rule__PUBLIC__Group__1
            {
            pushFollow(FOLLOW_5);
            rule__PUBLIC__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__PUBLIC__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__PUBLIC__Group__0"


    // $ANTLR start "rule__PUBLIC__Group__0__Impl"
    // InternalIDFComponentDsl.g:906:1: rule__PUBLIC__Group__0__Impl : ( 'public:' ) ;
    public final void rule__PUBLIC__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:910:1: ( ( 'public:' ) )
            // InternalIDFComponentDsl.g:911:1: ( 'public:' )
            {
            // InternalIDFComponentDsl.g:911:1: ( 'public:' )
            // InternalIDFComponentDsl.g:912:2: 'public:'
            {
             before(grammarAccess.getPUBLICAccess().getPublicKeyword_0()); 
            match(input,18,FOLLOW_2); 
             after(grammarAccess.getPUBLICAccess().getPublicKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__PUBLIC__Group__0__Impl"


    // $ANTLR start "rule__PUBLIC__Group__1"
    // InternalIDFComponentDsl.g:921:1: rule__PUBLIC__Group__1 : rule__PUBLIC__Group__1__Impl ;
    public final void rule__PUBLIC__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:925:1: ( rule__PUBLIC__Group__1__Impl )
            // InternalIDFComponentDsl.g:926:2: rule__PUBLIC__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__PUBLIC__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__PUBLIC__Group__1"


    // $ANTLR start "rule__PUBLIC__Group__1__Impl"
    // InternalIDFComponentDsl.g:932:1: rule__PUBLIC__Group__1__Impl : ( ( rule__PUBLIC__NameAssignment_1 ) ) ;
    public final void rule__PUBLIC__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:936:1: ( ( ( rule__PUBLIC__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:937:1: ( ( rule__PUBLIC__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:937:1: ( ( rule__PUBLIC__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:938:2: ( rule__PUBLIC__NameAssignment_1 )
            {
             before(grammarAccess.getPUBLICAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:939:2: ( rule__PUBLIC__NameAssignment_1 )
            // InternalIDFComponentDsl.g:939:3: rule__PUBLIC__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__PUBLIC__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getPUBLICAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__PUBLIC__Group__1__Impl"


    // $ANTLR start "rule__OVERRIDE_PATH__Group__0"
    // InternalIDFComponentDsl.g:948:1: rule__OVERRIDE_PATH__Group__0 : rule__OVERRIDE_PATH__Group__0__Impl rule__OVERRIDE_PATH__Group__1 ;
    public final void rule__OVERRIDE_PATH__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:952:1: ( rule__OVERRIDE_PATH__Group__0__Impl rule__OVERRIDE_PATH__Group__1 )
            // InternalIDFComponentDsl.g:953:2: rule__OVERRIDE_PATH__Group__0__Impl rule__OVERRIDE_PATH__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__OVERRIDE_PATH__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__OVERRIDE_PATH__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__OVERRIDE_PATH__Group__0"


    // $ANTLR start "rule__OVERRIDE_PATH__Group__0__Impl"
    // InternalIDFComponentDsl.g:960:1: rule__OVERRIDE_PATH__Group__0__Impl : ( 'override_path:' ) ;
    public final void rule__OVERRIDE_PATH__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:964:1: ( ( 'override_path:' ) )
            // InternalIDFComponentDsl.g:965:1: ( 'override_path:' )
            {
            // InternalIDFComponentDsl.g:965:1: ( 'override_path:' )
            // InternalIDFComponentDsl.g:966:2: 'override_path:'
            {
             before(grammarAccess.getOVERRIDE_PATHAccess().getOverride_pathKeyword_0()); 
            match(input,19,FOLLOW_2); 
             after(grammarAccess.getOVERRIDE_PATHAccess().getOverride_pathKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__OVERRIDE_PATH__Group__0__Impl"


    // $ANTLR start "rule__OVERRIDE_PATH__Group__1"
    // InternalIDFComponentDsl.g:975:1: rule__OVERRIDE_PATH__Group__1 : rule__OVERRIDE_PATH__Group__1__Impl ;
    public final void rule__OVERRIDE_PATH__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:979:1: ( rule__OVERRIDE_PATH__Group__1__Impl )
            // InternalIDFComponentDsl.g:980:2: rule__OVERRIDE_PATH__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__OVERRIDE_PATH__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__OVERRIDE_PATH__Group__1"


    // $ANTLR start "rule__OVERRIDE_PATH__Group__1__Impl"
    // InternalIDFComponentDsl.g:986:1: rule__OVERRIDE_PATH__Group__1__Impl : ( ( rule__OVERRIDE_PATH__NameAssignment_1 ) ) ;
    public final void rule__OVERRIDE_PATH__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:990:1: ( ( ( rule__OVERRIDE_PATH__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:991:1: ( ( rule__OVERRIDE_PATH__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:991:1: ( ( rule__OVERRIDE_PATH__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:992:2: ( rule__OVERRIDE_PATH__NameAssignment_1 )
            {
             before(grammarAccess.getOVERRIDE_PATHAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:993:2: ( rule__OVERRIDE_PATH__NameAssignment_1 )
            // InternalIDFComponentDsl.g:993:3: rule__OVERRIDE_PATH__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__OVERRIDE_PATH__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getOVERRIDE_PATHAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__OVERRIDE_PATH__Group__1__Impl"


    // $ANTLR start "rule__RULES__Group__0"
    // InternalIDFComponentDsl.g:1002:1: rule__RULES__Group__0 : rule__RULES__Group__0__Impl rule__RULES__Group__1 ;
    public final void rule__RULES__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1006:1: ( rule__RULES__Group__0__Impl rule__RULES__Group__1 )
            // InternalIDFComponentDsl.g:1007:2: rule__RULES__Group__0__Impl rule__RULES__Group__1
            {
            pushFollow(FOLLOW_4);
            rule__RULES__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__RULES__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__RULES__Group__0"


    // $ANTLR start "rule__RULES__Group__0__Impl"
    // InternalIDFComponentDsl.g:1014:1: rule__RULES__Group__0__Impl : ( 'rules:' ) ;
    public final void rule__RULES__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1018:1: ( ( 'rules:' ) )
            // InternalIDFComponentDsl.g:1019:1: ( 'rules:' )
            {
            // InternalIDFComponentDsl.g:1019:1: ( 'rules:' )
            // InternalIDFComponentDsl.g:1020:2: 'rules:'
            {
             before(grammarAccess.getRULESAccess().getRulesKeyword_0()); 
            match(input,20,FOLLOW_2); 
             after(grammarAccess.getRULESAccess().getRulesKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__RULES__Group__0__Impl"


    // $ANTLR start "rule__RULES__Group__1"
    // InternalIDFComponentDsl.g:1029:1: rule__RULES__Group__1 : rule__RULES__Group__1__Impl ;
    public final void rule__RULES__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1033:1: ( rule__RULES__Group__1__Impl )
            // InternalIDFComponentDsl.g:1034:2: rule__RULES__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__RULES__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__RULES__Group__1"


    // $ANTLR start "rule__RULES__Group__1__Impl"
    // InternalIDFComponentDsl.g:1040:1: rule__RULES__Group__1__Impl : ( ( rule__RULES__NameAssignment_1 ) ) ;
    public final void rule__RULES__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1044:1: ( ( ( rule__RULES__NameAssignment_1 ) ) )
            // InternalIDFComponentDsl.g:1045:1: ( ( rule__RULES__NameAssignment_1 ) )
            {
            // InternalIDFComponentDsl.g:1045:1: ( ( rule__RULES__NameAssignment_1 ) )
            // InternalIDFComponentDsl.g:1046:2: ( rule__RULES__NameAssignment_1 )
            {
             before(grammarAccess.getRULESAccess().getNameAssignment_1()); 
            // InternalIDFComponentDsl.g:1047:2: ( rule__RULES__NameAssignment_1 )
            // InternalIDFComponentDsl.g:1047:3: rule__RULES__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__RULES__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getRULESAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__RULES__Group__1__Impl"


    // $ANTLR start "rule__ComponentModel__ElementsAssignment"
    // InternalIDFComponentDsl.g:1056:1: rule__ComponentModel__ElementsAssignment : ( ruleType ) ;
    public final void rule__ComponentModel__ElementsAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1060:1: ( ( ruleType ) )
            // InternalIDFComponentDsl.g:1061:2: ( ruleType )
            {
            // InternalIDFComponentDsl.g:1061:2: ( ruleType )
            // InternalIDFComponentDsl.g:1062:3: ruleType
            {
             before(grammarAccess.getComponentModelAccess().getElementsTypeParserRuleCall_0()); 
            pushFollow(FOLLOW_2);
            ruleType();

            state._fsp--;

             after(grammarAccess.getComponentModelAccess().getElementsTypeParserRuleCall_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ComponentModel__ElementsAssignment"


    // $ANTLR start "rule__Description__NameAssignment_1"
    // InternalIDFComponentDsl.g:1071:1: rule__Description__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__Description__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1075:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1076:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1076:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1077:3: RULE_STRING
            {
             before(grammarAccess.getDescriptionAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getDescriptionAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Description__NameAssignment_1"


    // $ANTLR start "rule__VersionInfo__NameAssignment_1"
    // InternalIDFComponentDsl.g:1086:1: rule__VersionInfo__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__VersionInfo__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1090:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1091:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1091:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1092:3: RULE_STRING
            {
             before(grammarAccess.getVersionInfoAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getVersionInfoAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__VersionInfo__NameAssignment_1"


    // $ANTLR start "rule__URL__NameAssignment_1"
    // InternalIDFComponentDsl.g:1101:1: rule__URL__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__URL__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1105:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1106:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1106:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1107:3: RULE_STRING
            {
             before(grammarAccess.getURLAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getURLAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__URL__NameAssignment_1"


    // $ANTLR start "rule__COMMENT__NameAssignment_1"
    // InternalIDFComponentDsl.g:1116:1: rule__COMMENT__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__COMMENT__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1120:1: ( ( RULE_ID ) )
            // InternalIDFComponentDsl.g:1121:2: ( RULE_ID )
            {
            // InternalIDFComponentDsl.g:1121:2: ( RULE_ID )
            // InternalIDFComponentDsl.g:1122:3: RULE_ID
            {
             before(grammarAccess.getCOMMENTAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getCOMMENTAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMMENT__NameAssignment_1"


    // $ANTLR start "rule__DependenciesComp__FeaturesAssignment_1"
    // InternalIDFComponentDsl.g:1131:1: rule__DependenciesComp__FeaturesAssignment_1 : ( ruleFeature ) ;
    public final void rule__DependenciesComp__FeaturesAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1135:1: ( ( ruleFeature ) )
            // InternalIDFComponentDsl.g:1136:2: ( ruleFeature )
            {
            // InternalIDFComponentDsl.g:1136:2: ( ruleFeature )
            // InternalIDFComponentDsl.g:1137:3: ruleFeature
            {
             before(grammarAccess.getDependenciesCompAccess().getFeaturesFeatureParserRuleCall_1_0()); 
            pushFollow(FOLLOW_2);
            ruleFeature();

            state._fsp--;

             after(grammarAccess.getDependenciesCompAccess().getFeaturesFeatureParserRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__DependenciesComp__FeaturesAssignment_1"


    // $ANTLR start "rule__IDF__NameAssignment_1"
    // InternalIDFComponentDsl.g:1146:1: rule__IDF__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__IDF__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1150:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1151:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1151:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1152:3: RULE_STRING
            {
             before(grammarAccess.getIDFAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getIDFAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__IDF__NameAssignment_1"


    // $ANTLR start "rule__COMPONENT_NAME__NameAssignment_0"
    // InternalIDFComponentDsl.g:1161:1: rule__COMPONENT_NAME__NameAssignment_0 : ( RULE_ID ) ;
    public final void rule__COMPONENT_NAME__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1165:1: ( ( RULE_ID ) )
            // InternalIDFComponentDsl.g:1166:2: ( RULE_ID )
            {
            // InternalIDFComponentDsl.g:1166:2: ( RULE_ID )
            // InternalIDFComponentDsl.g:1167:3: RULE_ID
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getNameIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getCOMPONENT_NAMEAccess().getNameIDTerminalRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__NameAssignment_0"


    // $ANTLR start "rule__COMPONENT_NAME__FeaturesAssignment_2"
    // InternalIDFComponentDsl.g:1176:1: rule__COMPONENT_NAME__FeaturesAssignment_2 : ( ruleCOMPONENT_NAME_FEATURE ) ;
    public final void rule__COMPONENT_NAME__FeaturesAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1180:1: ( ( ruleCOMPONENT_NAME_FEATURE ) )
            // InternalIDFComponentDsl.g:1181:2: ( ruleCOMPONENT_NAME_FEATURE )
            {
            // InternalIDFComponentDsl.g:1181:2: ( ruleCOMPONENT_NAME_FEATURE )
            // InternalIDFComponentDsl.g:1182:3: ruleCOMPONENT_NAME_FEATURE
            {
             before(grammarAccess.getCOMPONENT_NAMEAccess().getFeaturesCOMPONENT_NAME_FEATUREParserRuleCall_2_0()); 
            pushFollow(FOLLOW_2);
            ruleCOMPONENT_NAME_FEATURE();

            state._fsp--;

             after(grammarAccess.getCOMPONENT_NAMEAccess().getFeaturesCOMPONENT_NAME_FEATUREParserRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__COMPONENT_NAME__FeaturesAssignment_2"


    // $ANTLR start "rule__PUBLIC__NameAssignment_1"
    // InternalIDFComponentDsl.g:1191:1: rule__PUBLIC__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__PUBLIC__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1195:1: ( ( RULE_ID ) )
            // InternalIDFComponentDsl.g:1196:2: ( RULE_ID )
            {
            // InternalIDFComponentDsl.g:1196:2: ( RULE_ID )
            // InternalIDFComponentDsl.g:1197:3: RULE_ID
            {
             before(grammarAccess.getPUBLICAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getPUBLICAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__PUBLIC__NameAssignment_1"


    // $ANTLR start "rule__OVERRIDE_PATH__NameAssignment_1"
    // InternalIDFComponentDsl.g:1206:1: rule__OVERRIDE_PATH__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__OVERRIDE_PATH__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1210:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1211:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1211:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1212:3: RULE_STRING
            {
             before(grammarAccess.getOVERRIDE_PATHAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getOVERRIDE_PATHAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__OVERRIDE_PATH__NameAssignment_1"


    // $ANTLR start "rule__RULES__NameAssignment_1"
    // InternalIDFComponentDsl.g:1221:1: rule__RULES__NameAssignment_1 : ( RULE_STRING ) ;
    public final void rule__RULES__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalIDFComponentDsl.g:1225:1: ( ( RULE_STRING ) )
            // InternalIDFComponentDsl.g:1226:2: ( RULE_STRING )
            {
            // InternalIDFComponentDsl.g:1226:2: ( RULE_STRING )
            // InternalIDFComponentDsl.g:1227:3: RULE_STRING
            {
             before(grammarAccess.getRULESAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            match(input,RULE_STRING,FOLLOW_2); 
             after(grammarAccess.getRULESAccess().getNameSTRINGTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__RULES__NameAssignment_1"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x000000000000F802L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000090020L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000090022L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000140000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000140002L});

}