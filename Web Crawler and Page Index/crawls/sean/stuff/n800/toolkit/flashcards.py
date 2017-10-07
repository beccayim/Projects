import toolkit
import gtk
import string
import random
import math
import os


#  Flashcards
#  By Sean Luke, Copyright 2007
#  Distributed under the Academic Free License 3.0
#       http://www.opensource.org/licenses/academic.php 
#
#  This application expect that you have one or more flashcard files located in the
#  directory "flash", which is in turn located in your Documents directory.
#  The files can be anywhere in the "flash" directory or in a subdirectory of
#  it, and other files can be there as well: the Flashcards app only looks for 
#  flashcard files. 
#
#  A flashcard file ends in the extension ".flashcard".  Its format is simple: a text
#  file whose first line is the "title" of the flashcard (to be displayed in the window),
#  whose second line is a number from 0.0 to 1.0, indicating aggressiveness for this file
#  (you should start with 0.5; FlashCards will change it later), and whose third and additional
#  lines are flashcard entries.
#
#  A flashcard entry is simply two strings separated by a tab.  The first string is the "question"
#  and the second string is the "answer".  For example: il padre<tab>dad<newline>
#  The order of the entries matters: earlier entries are more likely to be selected than later
#  ones.  The degree to which the application will follow this rule, rather than pick at random,
#  is determined by the aggressiveness value of the flashcard deck.  As you get cards correct, or
#  incorrect, the application will move them up or down in the ordering so they're more or less
#  likely to be selected in the future.  Thus the application will modify your flashcard files:
#  be prepared for this eventuality.



class Deck:
    dirty = False
    #Warning: these don't get set to new values -- you have to do that in initialization,
    #so if everyone appends to them it's just one big array.  Nasty bug.
    questions = []
    answers = []
    filename = None
    error = None
    name = ""
    aggressiveness = 0.5
    index = 0
    questionsOnly = False
    
    def open(self,filename):
        self.questions = []
        self.answers = []
        self.filename = filename
        fil = open(self.filename, "r")
        # First read in the name
        self.name = fil.readline().strip()
        # Next read in the aggressiveness
        line = fil.readline()
        try:
            self.aggressiveness = float(fil.readline())
        except ValueError:
            if self.error!= None:
                self.error = "Aggressiveness line (second line in file) is not a number:\n%s" % line
        # Now read in the lines
        for line in fil.readlines():
            lin = line.strip()
            if len(lin) > 0 and lin[0] != '%':
                s = lin.split("\t")
                l = len(s)
                if (l == 2):
                    self.questions.append(s[0].strip())
                    self.answers.append(s[1].strip())
                elif self.error == None:
                    self.error = "Line Must have exactly one tab:\n%s" % line
        fil.close()
        return self
    
    def save(self):
        if self.dirty:
            fil = open(self.filename, "w")
            fil.write("%s\n" % self.name)
            fil.write("%s\n" % self.aggressiveness)
            for i in range(len(self.questions)):
                fil.write("%s\t%s\n" % (self.questions[i], self.answers[i]))
            fil.close()
        self.dirty = False
        return self
    
    def reinsert(self, index, newindex):
        if newindex < 0: newindex = 0
        if newindex >= len(self.questions): newindex = len(self.questions) - 1
        q = self.questions[index]
        self.questions = self.questions[:index] + self.questions[index+1:]
        self.questions = self.questions[:newindex] + [q] + self.questions[newindex:]
        a = self.answers[index]
        self.answers = self.answers[:index] + self.answers[index+1:]
        self.answers = self.answers[:newindex] + [a] + self.answers[newindex:]
        self.dirty = True
        return self
    
    def pickRandom(self):
        i1 = random.random()*len(self.questions)
        if (random.random() < self.aggressiveness):
            i2 = random.random()*len(self.questions)
            return int(min(i1,i2))
        else:
            return int(i1)
            
    def getCard(self):
        self.index = self.pickRandom()
        return self 
        #(self.questions[self.index], self.answers[self.index])
    
    def correct(self):
        self.reinsert(self.index, self.index + int(math.ceil((len(self.questions) - self.index - 1) / 2.0)))
        return self
    
    def incorrect(self):
        self.reinsert(self.index, self.index /2)
        return self
    
    def setAggressiveness(self, value):
        self.aggressiveness = value


def endsIn(str, substr):
    "Returns true if str ends in substr"
    return str.rfind(substr) == len(str) - len(substr)

def allFlashFiles():
    "Generator for all files which end in '.flashcard'"
    for path, dirs, files in os.walk(os.path.abspath("/home/user/MyDocs/.documents/flash")):
        for file in files:
            if endsIn(os.path.join(path, file), ".flashcard"):
                yield os.path.join(path, file)

def buildFlashCards():
    decks = []
    for filename in allFlashFiles():
        print filename
        decks.append(Deck().open(filename))
    return decks

def showOne(deck, question, answer, showQuestion=False):
    question.label.set_markup("<span size='x-large'> </span>")
    answer.label.set_markup("<span size='x-large'> </span>")
    if random.random() < 0.5 or showQuestion==True:
        question.label.set_markup("<span size='x-large'>%s</span>" % deck.questions[deck.index])
    else:
        answer.label.set_markup("<span size='x-large'>%s</span>" % deck.answers[deck.index])

def showBoth(deck, question, answer):
    question.label.set_markup("<span size='x-large'> </span>")
    answer.label.set_markup("<span size='x-large'> </span>")
    question.label.set_markup("<span size='x-large'>%s</span>" % deck.questions[deck.index])
    answer.label.set_markup("<span size='x-large'>%s</span>" % deck.answers[deck.index])

def setQuestionForm(deck, form):
    if form==0:
        deck.questionsOnly = False
    else:
        deck.questionsOnly = True

def buildButtons(deck, question, answer):
    yes = toolkit.Button("Yes", action=lambda state: showOne(deck.correct().getCard(), question, answer))
    no = toolkit.Button("No", action=lambda state: showOne(deck.incorrect().getCard(), question, answer))
    show = toolkit.Button("Show", action=lambda state: showBoth(deck, question, answer))
    s = toolkit.Slider(currentValue=deck.aggressiveness, action=lambda value: deck.setAggressiveness(value))
    val = 0
    # For now this isn't persistent :-(
    if deck.questionsOnly == True:
        val = 1
    both = toolkit.Button(["Both", "Question"], currentValue = val, action=lambda value: setQuestionForm(deck, value))
    showOne(deck, question, answer)
    return [yes, no, show, s, both]

decks = buildFlashCards()
toolkit.loadPersistenceData("/home/user/MyDocs/.documents/flash/persistenceData")
n = toolkit.Notebook(name="decks")
w = toolkit.MainWindow("Flash Cards", child=n)
for deck in decks:
    v = toolkit.Box(vertical=True)
    n.add(v, tab=deck.name)
    question = toolkit.Label("<span size='x-large'> </span>", wrappable=True)
    answer = toolkit.Label("<span size='x-large'> </span>", wrappable=True)
    v.add(question)
    v.add(answer)
    h = toolkit.Box()
    v.add(h, expand=True)
    # I have to do this because variables inside a for loop aren't by
    # default local.  Stupid stupid stupid.
    yes, no, show, s, both = buildButtons(deck, question, answer)    
    h.add(yes, expand=True)
    h.add(no, expand=True)
    h.add(show, expand=True)
    h2 = toolkit.Box()
    v.add(h2)
    h2.add(toolkit.Label("Aggressiveness"))
    h2.add(s, expand=True)
    h2.add(both)

w.show()
gtk.main()
for deck in decks:
    deck.save()

toolkit.savePersistenceData()



