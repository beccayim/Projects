;;;; SNARF Version 3.1
;;;; A simple prototype-style object-oriented programming language
;;;; extension to Lisp, in the theme of SELF, NewtonScript,
;;;; and (ugh) JavaScript and Python.  Much simpler, more elegant, 
;;;; and slower than CLOS.  :-)
;;;; http://cs.gmu.edu/~sean/lisp/snarf.lisp
;;;;
;;;; See instructions and documentation at the file's end.
;;;; 
;;;; Copyright 2006 by Sean Luke
;;;; This file is licensed under the Academic Free License version 3.0
;;;; which is included at the end of the file.


(defpackage :snarf (:use :common-lisp-user :common-lisp))
  
(in-package :snarf)
(export '(new-obj mapobj parent call-if-exists ?call
	  call super super-if-exists ?super slot-val
	  slot-obj mset meth meth-func func-meth
	  this meth-owner meth-name
	  set-original-slot *snarf-version*
	  *print-snarf-objects-verbosely*))

(defconstant *snarf-version* 3 "Snarf's Version Number")

(defparameter *print-snarf-objects-verbosely* nil)

(defmacro definline (name &rest stuff)
  "Defines a function and declares it to be inlined"  ;; convenient, no?
  `(progn (declaim (inline ,name))
	  (defun ,name ,@stuff)))

(defstruct (snarf-object (:print-object print-snarf-object)) slots parent)

(defstruct (snarf-method (:print-object print-snarf-method)) lambda arg-list)

(defmacro do-objects ((var obj &optional return) &rest body)
  "Iterates over each object from obj through its ancestors"
  (let ((x (gensym)) (o (gensym)))
    `(let ((,o ,obj))
       (do (,var (,x ,o (snarf-object-parent ,x)))
	   ((null ,x))  ;; as long as it's not null
	 (setf ,var ,x)
	 ,@body)
       ,return)))

(defmacro do-hashes ((var obj &optional return) &rest body)
  "Iterates over each hash table in obj and its ancestors"
  (let ((x (gensym)))
    `(do-objects (,x ,obj ,return)
		 (let ((,var (snarf-object-slots ,x)))
		   ,@body))))

(defmacro simple-error-check (object &rest body)
  "Wraps the body in an error check for a valid object.
Objects can be null."
  `(if (or (snarf-object-p ,object) (null ,object))
	       (progn ,@body)
	       (error (format nil "~a is not a snarf-object" ,object))))

(defmacro error-check (slot object &rest body)
  "Wraps the body in an error check for a valid slot name and
object.  Objects can be null.  Slot names must be symbols.
Does not check if the slot is actually *in* the object."
  `(if (symbolp ,slot)
        (simple-error-check ,object ,@body)
        (error (format nil "~a is not a valid snarf slot name (it must be a symbol)" ,slot))))


;;; PARENTS
;;; parent   (settable)

  (definline parent (object)
    "Returns the parent of an object, else NIL if the object has no parent."
    (snarf-object-parent object))

  (definline (setf parent) (parent object) 
    (unless (or (snarf-object-p parent) (null parent))
      (error (format nil "~a is not a snarf-object or NIL" parent)))
    (setf (snarf-object-parent object) parent))



;;; SLOTS
;;; slot     (settable)
;;; slot-obj
;;; exists
;;; set-original-slot
;;; remove-slot
;;; mapobj
;;; mset

(let ((retfail (gensym))) ;; failure symbol

  ;; no error check
  (definline -slot (slot object failed-return-value)
    (let (val)
      (do-hashes (hash object)
	(setf val (gethash slot hash retfail))
	(unless (eq val retfail) (return-from -slot val)))
      failed-return-value))

  ;; with error check
  (definline slot-val (slot object &optional (failed-return-value retfail))
    "Looks up a slot in an object.  If the slot exists, its value is returned.
Else if failed-return-value is defined, and the slot does not exist, then
failed-return-value is returned.  Else an unknown-slot error is generated.
This function searches the object, then its parent hierarchy, looking for the slot.
Can be used with SETF.  The setf-version sets the slot directly in the object,
not the parent(s)."
    (error-check
     slot object
     (let ((retval (-slot slot object failed-return-value)))
       (if (eq retval retfail)
	   (error "Unknown slot ~a in object ~a" slot object)
	   retval))))
  
  (definline exists (slot object)
    "Returns T if a slot exists in an object, or one of its parents, else
returns NIL."
    (error-check
     slot object
     (not (eq (-slot slot object retfail) retfail))))

  ;; no error check
  (definline (setf -slot) (val slot object failed-return-value)
    (declare (ignore failed-return-value))
    (setf (gethash slot (snarf-object-slots object)) val))

  ;; with error check
  (definline (setf slot-val) (val slot object &optional failed-return-value)
    (error-check
     slot object
     (setf (-slot slot object failed-return-value) val)))

  ;; no error check
  (definline -slot-obj (slot object)
    (let (val (cur object))
      (do-hashes (x object nil)
	(setf val (gethash slot x retfail))
	(when (not (eq val retfail)) (return-from -slot-obj cur))
	(pop cur))))

  ;; with error check
  (definline slot-obj (slot object)
    "Searches up the parent chain looking for the object that first contains
the given slot, and returns that object, or NIL if no such object is found."
    (error-check
     slot object
     (-slot-obj slot object)))

  (definline set-original-slot (slot object val &optional (set-anyway nil) (failed-return-value retfail))
    "Searches up the parent chain from OBJECT until it finds
the object which defined SLOT, then sets the slot in that object
to VAL and returns VAL.  If there is no such slot in OBJECT or any of its ancestors,
then SET-ANYWAY is T, SLOT will be simply be set to VAL in the OBJECT itself, and returns VAL.
If SET-ANYWAY is NIL and no slot was found, then issues an error,
or if FAILED-RETURN-VALUE is defined, returns that value."
    (error-check
     slot object
     (if (null object)
	 (error (format nil "Slot ~a cannot be set in ~a" slot object))
	 (let ((orig-object (-slot-obj slot object)))
	   (if orig-object
	       (setf (-slot slot orig-object nil) val)
	       (if set-anyway
		   (setf (-slot slot object nil) val)
		   (if (eq failed-return-value retfail)
		       (error (format nil "Slot ~a does not exist in object ~a" slot object))
		       failed-return-value)))))))

  (definline remove-slot (slot object &optional (deeply nil) (failed-return-value retfail))
    "If DEEPLY it NIL (the default), then deletes and returns the SLOT, if it exists, directly in OBJECT.
If DEEPLY is T, then deletes and returns the SLOT, if it exists, in OBJECT or
the first ancestor that contains it.  In either case, if no slot is found, then issues
an error, or if FAILED-RETURN-VALUE is defined, returns that value."
    (error-check
     slot object
     (if (null object)
	 (error (format nil "Slot ~a cannot be deleted in ~a" slot object))
	 (let (retval)
	   (if deeply
	       (do-hashes (hash object)
		 (setf retval (gethash slot hash retfail))
		 (if (remhash slot hash) (return)))
	       (let ((hash (snarf-object-slots object)))
		 (setf retval (gethash slot hash retfail))
		 (remhash slot hash)))
	   (if (eq retval retfail)
	       (if (eq failed-return-value retfail)
		   (error (format nil "Slot ~a does not exist in object ~a" slot object))))
	   retval)))))
 
  (defun mapobj (func obj &optional (deeply nil))
    "Maps FUNC over all of the slots in OBJ.  FUNC must take THREE
arguments: (1) the object defining the slot, (2) the slot name,
and (3) the slot value.  If DEEPLY is NIL (the default), then only slots in OBJ
are mapped.  Otherwise slots in OBJ and all parent objects are
mapped, except for slots in parent objects that cannot be seen
in OBJ because they are overridden.  That is, only one slot
is mapped for a given slot name.  nil is returned."
    (simple-error-check obj
     (if deeply
	   (let (slot-objs slots vals (history (make-hash-table :test #'eq)))
	      (do-hashes (hash obj)
	       (maphash #'(lambda (slot val)
			 (unless (gethash slot history)
			     (push obj slot-objs)
			      (push slot slots)
			      (push val vals)
			      (setf (gethash slot history) t)))
		       hash))
	   (mapcar func (nreverse slot-objs) (nreverse slots) (nreverse vals)))
	 (maphash #'(lambda (slot val) (funcall func obj slot val))
		  (snarf-object-slots obj)))))


  (defmacro mset (obj &rest slots)
    "Sets multiple slots in OBJ.  SLOTS is a list of (slotsymbol slotvalue)
pairs.  The object is returned."
  ;;; build the slots  
    (let ((o (gensym))) 
      (let ((s (mapcar #'(lambda (slot)
			   (when (or (not (listp slot))
				     (not (= (length slot) 2)))
			     (error (format nil "Bad slot definition ~a" slot)))
			   `(setf (slot-val ',(first slot) ,o) ,(second slot)))
		       slots)))
	`(let ((,o ,obj)) ,@s ,o))))


;;; PRINTING
;;; print-snarf-object (privately used)
;;; print-snarf-method (privately used)

  (defun print-snarf-object (object stream)
    (format stream "#<snarf-object~a~{ ~a~}>"
	    (if (parent object) "" " root")
	    (if *print-snarf-objects-verbosely*
		  (let (bag)
		      (mapobj #'(lambda (obj slot val)
		          (declare (ignore obj val)) (push slot bag))
			object nil)
			 bag)
		nil)))

  (defun print-snarf-method (object stream)
    (format stream "#<snarf-method~{ ~a~}>"
	    (snarf-method-arg-list object)))



;;; METHOD DEFINITIONS
;;; meth
;;; this
;;; meth-owner
;;; meth-name

  (let ((this (gensym))
	(meth-name (gensym))
	(meth-owner (gensym)))

    (defmacro this () 
      "Returns a pointer to the object on which the method was called"
      `((identity ,this)))

    (defmacro meth-name ()
      "Returns a symbol representing the name of the method when it was called"
      `((identity ,meth-name)))
  
    (defmacro meth-owner ()
      "Returns a pointer to THIS or the ancestor of THIS which actually contains the method"
      `((identity ,meth-owner)))

    (defmacro meth ((&rest arguments) &rest body)
      "Produces a method of the given arguments and body."
      `(snarf::make-snarf-method :arg-list ',arguments :lambda 
	#'(lambda (,this ,meth-owner ,meth-name ,@arguments)
	    (declare (ignore ,this ,meth-owner ,meth-name)) ;; in case we don't use them
	    ,@body))))

;;; METHOD CALLS
;;; call-if-exists
;;; call
;;; ?call
;;; super-if-exists
;;; super
;;; ?super

(let ((retfail (gensym)))
  (definline call-if-exists (method-name object return-if-does-not-exist &rest args)
    "Looks up the slot called METHOD-NAME in object, and calls it as a method,
passing in args.  If the method does not exist, returns return-if-does-not-exist."
    (error-check
     method-name object
     (let ((slot (-slot method-name object retfail)))
       (if (not (typep 'snarf-method slot)) return-if-does-not-exist
	   (apply (snarf-method-lambda slot) object object method-name args))))))

  (definline ?call (method-name object &rest args)
    "Looks up the slot called METHOD-NAME in object, and calls it as a method,
passing in args.  If the method does not exist, returns NIL."
    (apply #'call-if-exists method-name object nil args))

(let ((retfail (gensym)))
  (definline call (method-name object &rest args)
    "Looks up the slot called METHOD-NAME in object, and calls it as a method,
passing in args.  If the method does not exist, an error is signalled."
    (let ((retval (apply #'call-if-exists method-name object retfail args)))
      (if (eq retval retfail)
	  (error (format nil "No such method ~a in object ~a" method-name object))
	  retval))))

  (defmacro super-if-exists (return-if-does-not-exist &rest args)
    "Calls the method overridden by my method, passing in args.
Returns return-if-does-not-exist if the overridden method does not exist."
    (let ((method (gensym)) (parent (gensym)) (retfail (gensym)))
      `(let ((,parent (parent (meth-owner))))
	 (if (not ,parent)
	     ,return-if-does-not-exist
	     (let ((,method (slot-val (meth-name) ,parent ',retfail)))
	     (if (not (typep 'snarf-method ,method))
		 ,return-if-does-not-exist
		 (funcall (snarf::snarf-method-lambda ,method) (this) ,parent (meth-name) ,@args)))))))

  (defmacro ?super (&rest args)
    "Calls the method overridden by my method, passing in args.
Returns NIL if the overridden method does not exist."
    `(super-if-exists nil ,@args))

  (defmacro super (&rest args)
    "Calls the method overridden by my method, passing in args.
Signals an error if the overridden method does not exist."
    (let ((retval (gensym)) (retfail (gensym)))
      `(let ((,retval (super-if-exists ,retfail ,@args)))
	 (if (eq ,retval ,retfail)
	     (error (format nil "No such supermethod ~a for object ~a" (meth-name) (this)))
	     ,retval))))



;;; METHOD GENERATING UTILITIES
;;; meth-func
;;; func-meth

  (definline meth-func (method-name object)
    "Given a method name and an object, produces a function pointer which will,
when called with some ARGS, will call the method on the object, passing in the ARGS,
and returning the return value of the method."
    (error-check
     method-name object
     #'(lambda (&rest args) (apply #'call method-name object args))))

  (definline func-meth (func)
    "Given a function pointer, produces a method which can be added to an object.  Calling
this method will in turn call the function pointer, and return the return value of the function.
This method can be placed into any object, but because it's just a wrapper for a function,
the function has no access to the THIS variable or to any SUPER macros.  In some sense,
this method is thus a ``static method'' a-la Java."
    (meth (&rest arguments)
	  (apply func arguments)))



;;; OBJECT CREATION
;;; new

    (definline new-obj (&optional (parent nil) &rest init-arguments)
      "Creates an empty object, optionally setting its parent.  If
any INIT-ARGUMENTS are provided, then INIT is called on the object,
passing in the arguments.  Returns the new argument, plus the return
value of the INIT method (or null if INIT not called) as an optional
return value."
      (simple-error-check parent
       (let (retval (obj (make-snarf-object :slots (make-hash-table :test #'eq) :parent parent)))
	 (when init-arguments
	 (setf retval (apply #'call 'init obj init-arguments)))
	 (values obj retval))))




;;; OTHER STUFF INSIDE A METHOD
;;; @  macro
;;;     Forms: @var               Expands to (slot-val 'var this)
;;;            @(meth ... )       Expands to (call 'meth this ... )
;;; @? macro
;;;     Form:  @?(meth ... )      Expands to (?call 'meth this ... )
;;; #@ macro
;;;     Form:  #@meth             Expands to (meth-func 'meth this)


(set-macro-character
 #\@ #'(lambda (stream char)
	 (declare (ignore char))
	 (let ((first-char (read-char stream)))
	   (cond ((and (char-equal first-char #\?) (char-equal (peek-char nil stream) #\( ))
		  (let ((val (read stream t nil t)))
		    (if (consp val)
			(let ((v1 (first val)) (v2 (rest val)))
			  (cond ((symbolp (first val))
				 `(?call (quote ,v1) (this) ,@v2))
				(t (error (format nil "Bad method call @?~a" val)))))
		      (error (format nil "Bad method call @?~a" val)))))
		 ((or (char-equal first-char #\newline)
		      (char-equal first-char #\tab)
		      (char-equal first-char #\space)
		      (char-equal first-char #\linefeed)
		      (char-equal first-char #\return))  ;; not all lisps have whitespacep
		  (error (format nil "Whitespace after @")))
		 (t
		  (unread-char first-char stream)  ;; put the ? -- or whatever -- back
		  (let ((val (read stream t nil t)))
		    (cond ((symbolp val)
			   `(slot-val ',val (this)))
			  ((consp val)
			   (let ((v1 (first val)) (v2 (rest val)))
			     (cond ((symbolp (first val))
				    `(call (quote ,v1) (this) ,@v2))
				   (t (error (format nil "Bad method call @~a" val))))))
			  (t (error (format nil "Bad instance variable @~a" val))))))))))

(set-dispatch-macro-character
 #\# #\@ #'(lambda (stream subchar arg)
	     (declare (ignore subchar arg))
	     (let ((val (read stream t nil t)))
	       (if (symbolp val)
		    `(meth-func ,val (this))
		 (error (format nil "Must be a symbol in order to funcify: ~a" val))))))


#|
SNARF

Snarf is a very small, simple, typeless, prototype-style (non-class),
single-inheritance object-oriented programming language.  It's not phenominally
fast (its speed is largely dependent on the quality of the hash table implementation
on your system).  But I've found it to be quite handy.  The entire language is
defined in this file.

In Snarf, there are no classes.  There are only objects.  Objects are
simply dictionaries of <name, value> pairs.  Objects can have parent objects,
and inherit pairs from them.  The system presently uses single inheritance.

In Snarf parlance, a <name, value> pair is called a SLOT.  The name of the slot
is a symbol.  The value can be anything.
One particularly useful item to put into the value is a special kind of function,
which we will call a METHOD.  This is a model fairly similar to how Self,
JavaScript, NewtonScript, and Python handle things.   It is *very* different from,
and much simpler than, how matters are handled in CLOS.

Objects are created with the NEW-OBJ function. To make a blank object with
no parent, you just say:

(new-obj)

To make an object which inherits from another, you say:

(new-obj *parent-obj*)

You can get the parent object of an object with PARENT.
Parents themselves have parents, etc.  Thus an object has
a CHAIN OF ANCESTORS.

(parent *my-obj*)

The PARENT method is settable, but be careful to set it
only to real OBJECTs or to NIL.

(setf (parent *my-obj*) *another-parent-object*)



SLOTS

After you create an object, you can set a slot in it:

(setf (slot-val 'color *my-obj*) 'blue)

You get a slot like this:

(slot-val 'color *my-obj*)

...except that this function (unlike the setf version) will look up an
inherited parent slot if *my-obj* doesn't have it.  If there's no
slot or inherited slot called COLOR, all the way up the ancestor chain,
then an error is signalled.

You can tentatively get a slot value, returning a value if there's no
such slot (instead of generating an error):

(slot-val 'color *my-obj* 'return-this-if-failed-to-find)

This form isn't settable -- what would be the point?  To test the
existence of a slot, you say:

(exists 'color *my-obj*)

You can even have the system return that object in the ancestor chain
which actually holds the slot you're looking for in its hashtable, else
NIL.

(slot-obj 'color *my-obj*)

You can also iterate over the immediate slots in an object.  To print
all the immediate slots in an object, you can say:

(mapobj #'(lambda (object symbol value)
	    (format t "~%Object ~a has slot ~a with value ~a" object symbol value))
	*my-obj*)

If you'd like to iterate over all the slots in an object and its ancestors,
you can add a 't':

(mapobj #'(lambda (object symbol value)
	    (format t "~%Object ~a has slot ~a with value ~a" object symbol value))
	*my-obj* t)

This function only prints out one value for a given symbol: thus if an object
overrides ancestor's slots, the ancestor's slots will not be printed in this example.

To set multiple slots simultaneously in an object, the MSET macro is provided:

(mset *my-obj*
      (color 'red)
      (width 42)
      (print-function #'(lambda () (print "Hello"))))

This is particularly useful for setting slots in a brand-new object:

(setf *my-obj* (mset (new-obj *parent-obj*)
		     (color 'red)
		     (width 42)
		     (print-function #'(lambda () (print "Hello")))))

Sometimes you don't want to set slots in your object, but would prefer to set them
in the parent object from which you can see the slot.  This is most commonly done
to allow multiple objects to "share" a common slot among them.  Setting such a slot
could be done with something along the lines of:

(setf (slot-val 'the-slot-name (slot-obj 'the-slot-name *my-obj*)) 'the-new-value)

That's a little ugly, so the following function will do the same thing:

(set-orignal-slot 'the-slot-name *my-obj* 'the-new-value)

If there is no such slot, this will generate an error.  Instead you can have it
return an optional value on failure to find the slot:

(set-original-slot 'the-slot-name *my-obj* 'the-new-value 'my-failure-symbol)

If you'd like to remove a slot from an immediate object, you can do:

(remove-slot 'the-slot-name *my-obj*)

This removes the slot and returns its value.  Note that only the immediate object
is searched -- not any ancestors.  If instead you'd like the first visible slot
in the object or its ancestors to be removed -- that is, the slot that's used
when you say (slot-value ...), you can say:

(remove-slot 'the-slot-name *my-obj* t)

Ordinarily REMOVE-SLOT returns the value of the slot it had removed.  
If no such slot exists, an error is thrown unless you provide an optional value
on failure to find the slot:

(remove-slot 'the-slot-name *my-obj* t 'my-failure-symbol)

Ordinarily Snarf prints objects in a plain way:

#<snarf-object>

...or if the object doesn't inherit from anyone...

#<snarf-object root>

However if you say

(setf *print-snarf-objects-verbosely* t)

...now Snarf objects will print themselves including their slot names (including
inherited slot names):

#<snarf-object root: COLOR WIDTH PRINT-FUNCTION>




DEFINING METHODS

Slots can hold many things, but one of the most useful things are METHODS.

Methods are functions which are called in the context of some object.
Methods operate identically to ordinary functions, except that they have access to
special macros and variables which enable them to determine which object they are stored in,
and how to call overridden versions of themselves in parent objects.

Just as functions are created with DEFUN or LAMBDA, Methods are created with the
METH macro.  Here is an example of a method created and stored as a slot in an object.
In this example, the method is just a simple function which takes two arguments
and returns the sum of them.

(setf (slot-val 'my-function *my-obj*)
      (meth (arg1 arg2) (+ arg2 arg2)))

Methods also have access to the object whose context they are in.  Inside a method,
the object is provided with the (THIS) macro.  Here's an example of a method which
takes two arguments, adds them, sets the object's RESULT slot to the sum, and then
returns the sum:

(setf (slot-val 'my-function *my-obj*)
      (meth (arg1 arg2) (setf (slot-val 'result (this)) (+ arg1 arg2))))

A method also can call a method of the same name in a parent object which it had overridden.
To do this, the method can employ the SUPER macro.

(setf (slot-val 'my-function *my-obj*)
      (meth (arg1 arg2)
	    (print (super arg1 45))
	    (+ arg1 arg2)))

If there's no overridden version, calling SUPER will generate an error.
You can tentatively call SUPER-IF-EXISTS, which returns a special
value you provide in the situation that the overridden method doesn't
exist:

(setf (slot-val 'my-function *my-obj*)
      (meth (arg1 arg2)
	    (print (super-if-exists 'NOTHING arg1 45))
	    (+ arg1 arg2)))

There is a simplified version of SUPER-IF-EXISTS called ?SUPER which just
returns NIL if there's no overridden version:

(setf (slot-val 'my-function *my-obj*)
      (meth (arg1 arg2)
	    (print (?super arg1 45))
	    (+ arg1 arg2)))

Because methods are just lambda expressions, they work with closures
just like you'd expect.

Methods have access to two other macros. The (METH-NAME) macro returns the name of the slot
holding the method.  The (METH-OWNER) variable refers to the object which actually stores
the slot.  This object is either (THIS) or it is an ancestor of (THIS).

Methods are printed in a plain manner:

#<snarf-method: ARG1 ARG2>



CALLING METHODS

You call a method with the CALL function:

(call 'adder *my-obj* 4 7)

Methods do not have access to the THIS or SUPER facilities if you do not execute
them with CALL (for example, if you executed them with FUNCALL or APPLY).

You can also tentatively call the method.  If the method doesn't exist,
you can have a return value sent back instead of an error generated.
To do this, you use the CALL-IF-EXISTS function:

(call-if-exists 'adder *my-obj* *return-if-doesn't-exist-value* 4 7)

There is also a simplified version of CALL-IF-EXISTS, called ?CALL, which returns
nil if the method doesn't exist:

(?call 'adder *my-obj* 4 7)

Inside a method, the @ read-macro is defined to make self-reference
simpler.  For example, it can be used to call your own methods.  The
following two lines are identical:

@(adder 4 7)
(call 'adder this 4 7)

Inside a method, the @ read-macro can also be used to access slots
in the owning object.  Thus the following two lines are identical:

@foo
(slot-val 'foo this)

You can also use this in setf:

(setf @foo 4)
(setf (slot-val 'foo this) 4)

The @? read-macro variant permits you to call your own methods tentatively.
The following two lines are identical:

@?(adder 4 7)
(?call 'adder this 4 7)

Snarf also provides a function which takes a method and an object, and "wraps"
the object into a closure around the method so the method can be used like an
ordinary function in FUNCALL, APPLY, MAPCAR, etc. but still have access to the
THIS and SUPER etc. facilities:

(mapcar
 (meth-func 'adder *my-obj*)
 '(1 2 3 4 5)
 '(6 7 8 9 10))

There is also a shorthand macro #@ for self-referential versions of this.
The following lines are identical:

#@adder
(meth-func 'adder this)

Snarf also lets you wrap a function as a method using the FUNC-METH macro.
Since ordinary functions are NOT methods, they do not have access to the THIS,
METH-NAME, METH-OWNER, or various SUPER macros.  Thus if you do something like this:

(mset *my-obj*
      (my-printer (func-meth #'print)))

...this wraps the #'print function as a method that you can stick into a slot on
an object and call with CALL.  In some sense, this function-in-a-method is like
Java's static methods, which also do not have access to any object context even though
they reside in an object.



CONSTRUCTORS

Because objects are created dynamically, not stamped out of a class,
we have not talked about the notion of a CONSTRUCTOR yet.  Indeed, for many prototype-type
languages (like NewtonScript), there is no constructor at all, because what constructors
usually do is set default initial values for things and your parent already has those
things set for you.

But if you must!  If you have a slot called INIT with a method in it, then Snarf can
call this as a constructor if you so desire.  In all other ways, INIT will be treated
as an ordinary method and an ordinary slot.

You can have Snarf call the constructor as part of the NEW-OBJ function.  If you call:

(new-obj *parent-obj* 4 5 "hello")

...this will create a new object whose parent is *parent-obj*.  It will then call
the INIT method on this object passing in 4, 5, and "hello".  It then returns
two return values.  The primary return value is the new object.  The secondary
return value is the return value of the INIT method.

This is basically the same thing as:

(let ((new-object (new-obj *parent-obj*)))
  (let ((retval (?call 'init new-object 4 5 "hello")))
    (values new-object retval)))

Snarf does not have any notion of finalizers (destructors) at all.
|#







#|
Academic Free License ("AFL") v. 3.0

This Academic Free License (the "License") applies to any original work
of authorship (the "Original Work") whose owner (the "Licensor") has
placed the following licensing notice adjacent to the copyright notice
for the Original Work:

Licensed under the Academic Free License version 3.0

1) Grant of Copyright License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, for the duration of
the copyright, to do the following:

        a) to reproduce the Original Work in copies, either alone or as
           part of a collective work;

        b) to translate, adapt, alter, transform, modify, or arrange the
           Original Work, thereby creating derivative works ("Derivative
           Works") based upon the Original Work;

        c) to distribute or communicate copies of the Original Work and
           Derivative Works to the public, UNDER ANY LICENSE OF YOUR
           CHOICE THAT DOES NOT CONTRADICT THE TERMS AND CONDITIONS,
           INCLUDING LICENSOR'S RESERVED RIGHTS AND REMEDIES, IN THIS
           ACADEMIC FREE LICENSE;

        d) to perform the Original Work publicly; and

        e) to display the Original Work publicly.

2) Grant of Patent License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, under patent claims
owned or controlled by the Licensor that are embodied in the Original
Work as furnished by the Licensor, for the duration of the patents, to
make, use, sell, offer for sale, have made, and import the Original Work
and Derivative Works.

3) Grant of Source Code License. The term "Source Code" means the
preferred form of the Original Work for making modifications to it and
all available documentation describing how to modify the Original Work.
Licensor agrees to provide a machine-readable copy of the Source Code of
the Original Work along with each copy of the Original Work that
Licensor distributes. Licensor reserves the right to satisfy this
obligation by placing a machine-readable copy of the Source Code in an
information repository reasonably calculated to permit inexpensive and
convenient access by You for as long as Licensor continues to distribute
the Original Work.

4) Exclusions From License Grant. Neither the names of Licensor, nor the
names of any contributors to the Original Work, nor any of their
trademarks or service marks, may be used to endorse or promote products
derived from this Original Work without express prior permission of the
Licensor. Except as expressly stated herein, nothing in this License
grants any license to Licensor's trademarks, copyrights, patents, trade
secrets or any other intellectual property. No patent license is granted
to make, use, sell, offer for sale, have made, or import embodiments of
any patent claims other than the licensed claims defined in Section 2.
No license is granted to the trademarks of Licensor even if such marks
are included in the Original Work. Nothing in this License shall be
interpreted to prohibit Licensor from licensing under terms different
from this License any Original Work that Licensor otherwise would have a
right to license.

5) External Deployment. The term "External Deployment" means the use,
distribution, or communication of the Original Work or Derivative Works
in any way such that the Original Work or Derivative Works may be used
by anyone other than You, whether those works are distributed or
communicated to those persons or made available as an application
intended for use over a network. As an express condition for the grants
of license hereunder, You must treat any External Deployment by You of
the Original Work or a Derivative Work as a distribution under section
1(c).

6) Attribution Rights. You must retain, in the Source Code of any
Derivative Works that You create, all copyright, patent, or trademark
notices from the Source Code of the Original Work, as well as any
notices of licensing and any descriptive text identified therein as an
"Attribution Notice." You must cause the Source Code for any Derivative
Works that You create to carry a prominent Attribution Notice reasonably
calculated to inform recipients that You have modified the Original
Work.

7) Warranty of Provenance and Disclaimer of Warranty. Licensor warrants
that the copyright in and to the Original Work and the patent rights
granted herein by Licensor are owned by the Licensor or are sublicensed
to You under the terms of this License with the permission of the
contributor(s) of those copyrights and patent rights. Except as
expressly stated in the immediately preceding sentence, the Original
Work is provided under this License on an "AS IS" BASIS and WITHOUT
WARRANTY, either express or implied, including, without limitation, the
warranties of non-infringement, merchantability or fitness for a
particular purpose. THE ENTIRE RISK AS TO THE QUALITY OF THE ORIGINAL
WORK IS WITH YOU. This DISCLAIMER OF WARRANTY constitutes an essential
part of this License. No license to the Original Work is granted by this
License except under this disclaimer.

8) Limitation of Liability. Under no circumstances and under no legal
theory, whether in tort (including negligence), contract, or otherwise,
shall the Licensor be liable to anyone for any indirect, special,
incidental, or consequential damages of any character arising as a
result of this License or the use of the Original Work including,
without limitation, damages for loss of goodwill, work stoppage,
computer failure or malfunction, or any and all other commercial damages
or losses. This limitation of liability shall not apply to the extent
applicable law prohibits such limitation.

9) Acceptance and Termination. If, at any time, You expressly assented
to this License, that assent indicates your clear and irrevocable
acceptance of this License and all of its terms and conditions. If You
distribute or communicate copies of the Original Work or a Derivative
Work, You must make a reasonable effort under the circumstances to
obtain the express assent of recipients to the terms of this License.
This License conditions your rights to undertake the activities listed
in Section 1, including your right to create Derivative Works based upon
the Original Work, and doing so without honoring these terms and
conditions is prohibited by copyright law and international treaty.
Nothing in this License is intended to affect copyright exceptions and
limitations (including "fair use" or "fair dealing"). This License shall
terminate immediately and You may no longer exercise any of the rights
granted to You by this License upon your failure to honor the conditions
in Section 1(c).

10) Termination for Patent Action. This License shall terminate
automatically and You may no longer exercise any of the rights granted
to You by this License as of the date You commence an action, including
a cross-claim or counterclaim, against Licensor or any licensee alleging
that the Original Work infringes a patent. This termination provision
shall not apply for an action alleging patent infringement by
combinations of the Original Work with other software or hardware.

11) Jurisdiction, Venue and Governing Law. Any action or suit relating
to this License may be brought only in the courts of a jurisdiction
wherein the Licensor resides or in which Licensor conducts its primary
business, and under the laws of that jurisdiction excluding its
conflict-of-law provisions. The application of the United Nations
Convention on Contracts for the International Sale of Goods is expressly
excluded. Any use of the Original Work outside the scope of this License
or after its termination shall be subject to the requirements and
penalties of copyright or patent law in the appropriate jurisdiction.
This section shall survive the termination of this License.

12) Attorneys' Fees. In any action to enforce the terms of this License
or seeking damages relating thereto, the prevailing party shall be
entitled to recover its costs and expenses, including, without
limitation, reasonable attorneys' fees and costs incurred in connection
with such action, including any appeal of such action. This section
shall survive the termination of this License.

13) Miscellaneous. If any provision of this License is held to be
unenforceable, such provision shall be reformed only to the extent
necessary to make it enforceable.

14) Definition of "You" in This License. "You" throughout this License,
whether in upper or lower case, means an individual or a legal entity
exercising rights under, and complying with all of the terms of, this
License. For legal entities, "You" includes any entity that controls, is
controlled by, or is under common control with you. For purposes of this
definition, "control" means (i) the power, direct or indirect, to cause
the direction or management of such entity, whether by contract or
otherwise, or (ii) ownership of fifty percent (50%) or more of the
outstanding shares, or (iii) beneficial ownership of such entity.

15) Right to Use. You may use the Original Work in all ways not
otherwise restricted or conditioned by this License or by law, and
Licensor promises not to interfere with or be responsible for such uses
by You.

16) Modification of This License. This License is Copyright (c) 2005
Lawrence Rosen. Permission is granted to copy, distribute, or
communicate this License without modification. Nothing in this License
permits You to modify this License as applied to the Original Work or to
Derivative Works. However, You may modify the text of this License and
copy, distribute or communicate your modified version (the "Modified
License") and apply it to other original works of authorship subject to
the following conditions: (i) You may not indicate in any way that your
Modified License is the "Academic Free License" or "AFL" and you may not
use those names in the name of your Modified License; (ii) You must
replace the notice specified in the first paragraph above with the
notice "Licensed under <insert your license name here>" or with a notice
of your own that is not confusingly similar to the notice in this
License; and (iii) You may not claim that your original works are open
source software unless your Modified License has been approved by Open
Source Initiative (OSI) and You comply with its license review and
certification process.
|#
