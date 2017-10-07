;;; this version is fairly efficient -- for a less efficient, easier
;;; to understand version, see below.

(defmacro doseq ((var sequence-form &optional resultform) &rest body)
  "Same as dolist, but operates over any arbitrary sequence.

Evaluates sequence-form, which produces a sequence, and executes the 
body once for every element in the sequence. On each iteration, var is
bound to successive elements of the sequence. Upon completion, resultform 
is evaluated, and the value is returned. If resultform is omitted, the
result is nil."

  (let ((x (gensym)) 
        (y (gensym)))
    `(let ((,x ,sequence-form))
      (cond ((listp ,x)            ;;; HANDLE LISTS IN O(N)
	     (dolist (,var ,x ,resultform) ,@body))
	    ((simple-vector-p ,x)  ;;; SPECIALLY HANDLE SIMPLE-VECTORS
	     (let (,var) (dotimes (,y (length ,x) ,resultform)
	       (setf ,var (svref ,x ,y)) ,@body)))
	    ((stringp ,x)         ;;; SPECIALLY HANDLE STRINGS
	     (let (,var) (dotimes (,y (length ,x) ,resultform)
	       (setf ,var (char ,x ,y)) ,@body)))
	    ((typep ,x 'sequence)   ;;; HANDLE OTHER KINDS OF SEQUENCES
	     (let (,var) (dotimes (,y (length ,x) ,resultform)
	       (setf ,var (elt ,x ,y)) ,@body)))
	    (t                     ;;; NOT A SEQUENCE?
	     ,x      ;;; quiets compiler complaints about no use
	     (error "DOSEQ: ~a is not a sequence" ,x))))))


;; here's a smaller version of the same function which just breaks
;; sequences into lists (dolist) and random-access sequences (dotimes).
;; not as efficient because it uses elt.  Also, doesn't check to see if
;; it's a sequence.  But it's easier to understand.

(defmacro doseq ((var sequence-form &optional resultform) &rest body)
    "Same as dolist, but operates over any arbitrary sequence.

Evaluates sequence-form, which produces a sequence, and executes the
body once for every element in the sequence. On each iteration, var is
bound to successive elements of the sequence. Upon completion, resultform
is evaluated, and the value is returned. If resultform is omitted, the
result is nil."
    
    (let ((x (gensym))
	  (y (gensym)))
      `(let ((,x ,sequence-form))
	 (if (listp ,x)
	     (dolist (,var ,x ,resultform) ,@body)    ; more efficient for lists
	   (let (,var)                                ; handle seqs here
	     (dotimes (,y (length ,x) ,resultform)
	       (setf ,var (elt ,x ,y))
	       ,@body))))))

