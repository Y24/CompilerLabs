begin
  integer k;
  integer g;
  integer function (n);
    begin
      integer n;
      if n<=0 then f:=1;
      else f:=n*f(n-1);
    end;
  read(m);
  k:=f(m);
  write(f);
end