package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  List<Grade> getGradesByExam_Id(String examId);

  Grade getGradeByExamIdAndStudentCourseStudentIdAndStudentCourseCourseId(String exam_id,
                                                                          String student_id,
                                                                          String course_id);

  List<Grade> getGradesByStudentCourse(StudentCourse studentCourse);

  List<Grade> getGradesByExamId(Exam exam);
}
